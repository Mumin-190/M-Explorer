package me.mumin.android.files.provider.root

import java8.nio.file.Path
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.util.valueCompat
import java.io.IOException

interface RootablePath {
    fun isRootRequired(isAttributeAccess: Boolean): Boolean
}

private val rootStrategy: RootStrategy
    get() = if (isRunningAsRoot) RootStrategy.NEVER else if (Settings.ENABLE_ROOT_ACCESS.valueCompat) RootStrategy.ALWAYS else RootStrategy.NEVER

@Throws(IOException::class)
fun <T, R> callRootable(
    path: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T, block: T.() -> R
): R {
    path as? RootablePath ?: throw IllegalArgumentException("$path is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER -> localObject.block()
        RootStrategy.AUTOMATIC ->
            if (path.isRootRequired(isAttributeAccess)) {
                checkShizukuAndRun(rootObject, block)
            } else {
                localObject.block()
            }
        RootStrategy.ALWAYS -> checkShizukuAndRun(rootObject, block)
    }
}

@Throws(IOException::class)
fun <T, R> callRootable(
    path1: Path,
    path2: Path,
    isAttributeAccess: Boolean,
    localObject: T,
    rootObject: T,
    block: T.() -> R
): R {
    path1 as? RootablePath ?: throw IllegalArgumentException("$path1 is not a RootablePath")
    path2 as? RootablePath ?: throw IllegalArgumentException("$path2 is not a RootablePath")
    return when (rootStrategy) {
        RootStrategy.NEVER ->
            localObject.block()
        RootStrategy.AUTOMATIC ->
            if (path1.isRootRequired(isAttributeAccess)
                || path2.isRootRequired(isAttributeAccess)) {
                checkShizukuAndRun(rootObject, block)
            } else {
                localObject.block()
            }
        RootStrategy.ALWAYS ->
            checkShizukuAndRun(rootObject, block)
    }
}

@Throws(IOException::class)
private fun <T, R> checkShizukuAndRun(rootObject: T, block: T.() -> R): R {
    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
        try {
            val hasShizuku = rikka.shizuku.Shizuku.checkSelfPermission() == android.content.pm.PackageManager.PERMISSION_GRANTED
            if (hasShizuku && !rikka.shizuku.Shizuku.pingBinder() && !rikka.sui.Sui.isSui()) {
                throw IOException("Shizuku service is not alive")
            }
        } catch (e: Throwable) {
            // Ignore if Shizuku is not installed or initialized
        }
    }
    return rootObject.block()
}
