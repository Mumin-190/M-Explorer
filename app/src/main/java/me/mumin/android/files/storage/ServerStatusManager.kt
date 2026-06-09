package me.mumin.android.files.storage

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java8.nio.file.Path
import me.mumin.android.files.provider.common.newDirectoryStream
import java.util.concurrent.ConcurrentHashMap
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

object ServerStatusManager {
    data class StatusInfo(
        val status: String, // "Connected", "Offline", "Authentication Failed", "Connecting"
        val lastCheckTimestamp: Long,
        val lastSuccessfulConnection: Long,
        val lastFailedConnection: Long
    )

    private val cache = ConcurrentHashMap<String, StatusInfo>()
    private val mutexMap = ConcurrentHashMap<String, Mutex>()
    private const val CACHE_DURATION_MS = 45000L // 45 seconds

    private fun getMutex(serverId: String): Mutex {
        return mutexMap.computeIfAbsent(serverId) { Mutex() }
    }

    fun getCachedStatus(serverId: String): StatusInfo? {
        return cache[serverId]
    }

    suspend fun checkServerStatus(
        context: Context,
        storage: Storage,
        force: Boolean = false
    ): StatusInfo {
        val serverId = storage.id.toString()
        val now = System.currentTimeMillis()
        
        val cached = cache[serverId]
        if (!force && cached != null && (now - cached.lastCheckTimestamp < CACHE_DURATION_MS) && cached.status != "Connecting") {
            return cached
        }

        val mutex = getMutex(serverId)
        return mutex.withLock {
            val cachedInner = cache[serverId]
            if (!force && cachedInner != null && (now - cachedInner.lastCheckTimestamp < CACHE_DURATION_MS) && cachedInner.status != "Connecting") {
                return@withLock cachedInner
            }

            val prevSuccess = cachedInner?.lastSuccessfulConnection ?: 0L
            val prevFailed = cachedInner?.lastFailedConnection ?: 0L
            
            cache[serverId] = StatusInfo("Connecting", now, prevSuccess, prevFailed)

            val resultState = withContext(Dispatchers.IO) {
                when (storage) {
                    is FtpServer -> FtpServerAuthenticator.addTransientServer(storage)
                    is SftpServer -> SftpServerAuthenticator.addTransientServer(storage)
                    is SmbServer -> SmbServerAuthenticator.addTransientServer(storage)
                    is WebDavServer -> WebDavServerAuthenticator.addTransientServer(storage)
                }

                try {
                    val path = storage.path
                    if (path == null) {
                        "OFFLINE"
                    } else {
                        path.fileSystem.use {
                            path.newDirectoryStream().use { stream ->
                                stream.toList()
                            }
                        }
                        "CONNECTED"
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    val message = e.message?.lowercase() ?: ""
                    if (message.contains("auth") || message.contains("login") || message.contains("credential") || 
                        message.contains("permission") || message.contains("access denied") || message.contains("530") ||
                        e is java.lang.SecurityException) {
                        "AUTH_FAILED"
                    } else {
                        "OFFLINE"
                    }
                } finally {
                    when (storage) {
                        is FtpServer -> FtpServerAuthenticator.removeTransientServer(storage)
                        is SftpServer -> SftpServerAuthenticator.removeTransientServer(storage)
                        is SmbServer -> SmbServerAuthenticator.removeTransientServer(storage)
                        is WebDavServer -> WebDavServerAuthenticator.removeTransientServer(storage)
                    }
                }
            }

            val checkEnd = System.currentTimeMillis()
            val finalStatus = when (resultState) {
                "CONNECTED" -> "Connected"
                "AUTH_FAILED" -> "Authentication Failed"
                else -> "Offline"
            }

            val successTimestamp = if (finalStatus == "Connected") checkEnd else prevSuccess
            val failedTimestamp = if (finalStatus != "Connected") checkEnd else prevFailed

            if (finalStatus == "Connected") {
                val prefs = context.getSharedPreferences("server_last_seen", Context.MODE_PRIVATE)
                prefs.edit().putLong(serverId, checkEnd).apply()
            }

            val info = StatusInfo(finalStatus, checkEnd, successTimestamp, failedTimestamp)
            cache[serverId] = info
            info
        }
    }
}
