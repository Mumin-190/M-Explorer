package me.mumin.android.files.app

import android.os.AsyncTask
import android.os.Build
import android.webkit.WebView
import jcifs.context.SingletonContext
import me.mumin.android.files.BuildConfig
import me.mumin.android.files.coil.initializeCoil
import me.mumin.android.files.filejob.fileJobNotificationTemplate
import me.mumin.android.files.ftpserver.ftpServerServiceNotificationTemplate
import me.mumin.android.files.sftpserver.sftpServerServiceNotificationTemplate
import me.mumin.android.files.hiddenapi.HiddenApi
import me.mumin.android.files.provider.FileSystemProviders
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.storage.FtpServerAuthenticator
import me.mumin.android.files.storage.SftpServerAuthenticator
import me.mumin.android.files.storage.SmbServerAuthenticator
import me.mumin.android.files.storage.StorageVolumeListLiveData
import me.mumin.android.files.storage.WebDavServerAuthenticator
import me.mumin.android.files.theme.custom.CustomThemeHelper
import me.mumin.android.files.theme.night.NightModeHelper
import java.util.Properties
import me.mumin.android.files.provider.ftp.client.Client as FtpClient
import me.mumin.android.files.provider.sftp.client.Client as SftpClient
import me.mumin.android.files.provider.smb.client.Client as SmbClient
import me.mumin.android.files.provider.webdav.client.Client as WebDavClient

val appInitializers = listOf(
    ::initializeCrashlytics,
    ::disableHiddenApiChecks,
    ::initializeWebViewDebugging,
    ::initializeCoil,
    ::initializeFileSystemProviders,
    ::upgradeApp,
    ::initializeLiveDataObjects,
    ::initializeCustomTheme,
    ::initializeNightMode,
    ::createNotificationChannels
)

private fun initializeCrashlytics() {
//#ifdef NONFREE
    me.mumin.android.files.nonfree.CrashlyticsInitializer.initialize()
//#endif
}

private fun disableHiddenApiChecks() {
    HiddenApi.disableHiddenApiChecks()
}

private fun initializeWebViewDebugging() {
    if (BuildConfig.DEBUG) {
        WebView.setWebContentsDebuggingEnabled(true)
    }
}

private fun initializeFileSystemProviders() {
    FileSystemProviders.install()
    FileSystemProviders.overflowWatchEvents = true
    // SingletonContext.init() calls NameServiceClientImpl.initCache() which connects to network.
    AsyncTask.THREAD_POOL_EXECUTOR.execute {
        SingletonContext.init(
            Properties().apply {
                setProperty("jcifs.netbios.cachePolicy", "0")
                setProperty("jcifs.smb.client.maxVersion", "SMB1")
            }
        )
    }
    FtpClient.authenticator = FtpServerAuthenticator
    SftpClient.authenticator = SftpServerAuthenticator
    SmbClient.authenticator = SmbServerAuthenticator
    WebDavClient.authenticator = WebDavServerAuthenticator
}

private fun initializeLiveDataObjects() {
    // Force initialization of LiveData objects so that it won't happen on a background thread.
    StorageVolumeListLiveData.value
    Settings.FILE_LIST_DEFAULT_DIRECTORY.value
}

private fun initializeCustomTheme() {
    CustomThemeHelper.initialize(application)
}

private fun initializeNightMode() {
    NightModeHelper.initialize(application)
}

private fun createNotificationChannels() {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        notificationManager.createNotificationChannels(
            listOf(
                backgroundActivityStartNotificationTemplate.channelTemplate,
                fileJobNotificationTemplate.channelTemplate,
                ftpServerServiceNotificationTemplate.channelTemplate,
                sftpServerServiceNotificationTemplate.channelTemplate
            ).map { it.create(application) }
        )
    }
}
