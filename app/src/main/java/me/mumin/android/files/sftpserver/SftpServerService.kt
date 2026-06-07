/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

package me.mumin.android.files.sftpserver

import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.annotation.WorkerThread
import androidx.core.content.ContextCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import me.mumin.android.files.compat.mainExecutorCompat
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.util.WakeWifiLock
import me.mumin.android.files.util.valueCompat
import org.apache.sshd.server.SshServer
import org.apache.sshd.server.keyprovider.SimpleGeneratorHostKeyProvider
import org.apache.sshd.sftp.server.SftpSubsystemFactory
import org.apache.sshd.server.auth.password.PasswordAuthenticator
import java.io.File
import java.nio.file.Paths
import java.util.concurrent.Executors

class SftpServerService : Service() {
    private var state = State.STOPPED
        set(value) {
            field = value
            _stateLiveData.value = value
        }

    private lateinit var wakeWifiLock: WakeWifiLock
    private lateinit var notification: SftpServerNotification
    private val executorService = Executors.newSingleThreadExecutor()
    private var sshServer: SshServer? = null

    override fun onCreate() {
        super.onCreate()
        wakeWifiLock = WakeWifiLock(SftpServerService::class.java.simpleName)
        notification = SftpServerNotification(this)
        executeStart()
    }

    override fun onBind(intent: Intent): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onDestroy() {
        super.onDestroy()
        executeStop()
        executorService.shutdown()
    }

    private fun executeStart() {
        if (state == State.STARTING || state == State.RUNNING) {
            return
        }
        wakeWifiLock.isAcquired = true
        notification.startForeground()
        state = State.STARTING
        executorService.execute { doStart() }
    }

    private fun executeStop() {
        if (state == State.STOPPING || state == State.STOPPED) {
            return
        }
        state = State.STOPPING
        executorService.execute { doStop() }
        notification.stopForeground()
        wakeWifiLock.isAcquired = false
    }

    @WorkerThread
    private fun postState(state: State) {
        mainExecutorCompat.execute { this.state = state }
    }

    @WorkerThread
    private fun doStart() {
        val port = Settings.SFTP_SERVER_PORT.valueCompat
        val username = Settings.FTP_SERVER_USERNAME.valueCompat
        val password = Settings.FTP_SERVER_PASSWORD.valueCompat
        val homeDir = Settings.FTP_SERVER_HOME_DIRECTORY.valueCompat.toString()

        try {
            System.setProperty("user.home", filesDir.absolutePath)
            org.apache.sshd.common.util.io.PathUtils.setUserHomeFolderResolver { filesDir.toPath() }
            sshServer = SshServer.setUpDefaultServer().apply {
                this.port = port
                keyPairProvider = SimpleGeneratorHostKeyProvider(File(filesDir, "sftp_hostkey.ser").toPath())
                subsystemFactories = listOf(SftpSubsystemFactory())
                passwordAuthenticator = PasswordAuthenticator { u, p, session ->
                    if (password.isEmpty()) {
                        true
                    } else {
                        u == username && p == password
                    }
                }
                fileSystemFactory = org.apache.sshd.common.file.virtualfs.VirtualFileSystemFactory(
                    Paths.get(homeDir)
                )
            }
            sshServer?.start()
        } catch (e: Exception) {
            e.printStackTrace()
            postState(State.STOPPED)
            return
        }
        postState(State.RUNNING)
    }

    @WorkerThread
    private fun doStop() {
        try {
            sshServer?.stop()
            sshServer = null
        } catch (e: Exception) {
            e.printStackTrace()
        }
        postState(State.STOPPED)
    }

    companion object {
        private val _stateLiveData = MutableLiveData(State.STOPPED)
        val stateLiveData: LiveData<State>
            get() = _stateLiveData

        fun start(context: Context) {
            ContextCompat.startForegroundService(
                context, Intent(context, SftpServerService::class.java)
            )
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, SftpServerService::class.java))
        }

        fun toggle(context: Context) {
            when (val state = _stateLiveData.valueCompat) {
                State.STARTING, State.STOPPING -> {}
                State.RUNNING -> stop(context)
                State.STOPPED -> start(context)
                else -> throw AssertionError(state)
            }
        }
    }

    enum class State {
        STARTING,
        RUNNING,
        STOPPING,
        STOPPED
    }
}
