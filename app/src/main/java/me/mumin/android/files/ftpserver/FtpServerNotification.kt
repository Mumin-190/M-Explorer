package me.mumin.android.files.ftpserver

import android.app.PendingIntent
import android.app.Service
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import me.mumin.android.files.R
import me.mumin.android.files.app.NotificationIds
import me.mumin.android.files.compat.stopForegroundCompat
import me.mumin.android.files.util.NotificationChannelTemplate
import me.mumin.android.files.util.NotificationTemplate
import me.mumin.android.files.util.createIntent

val ftpServerServiceNotificationTemplate =
    NotificationTemplate(
        NotificationChannelTemplate(
            "ftp_server",
            R.string.notification_channel_ftp_server_name,
            NotificationManagerCompat.IMPORTANCE_LOW,
            descriptionRes = R.string.notification_channel_ftp_server_description,
            showBadge = false
        ),
        colorRes = R.color.color_primary,
        smallIcon = R.drawable.notification_icon,
        contentTitleRes = R.string.ftp_server_notification_title,
        ongoing = true,
        onlyAlertOnce = true,
        category = NotificationCompat.CATEGORY_SERVICE,
        priority = NotificationCompat.PRIORITY_LOW
    )

class FtpServerNotification(private val service: Service) {
    private val receiver = FtpServerUrl.createChangeReceiver(service) { doStartForeground() }

    fun startForeground() {
        doStartForeground()
        receiver.register()
    }

    private fun doStartForeground() {
        val contextText = FtpServerUrl.getUrl()
            ?: service.getString(R.string.ftp_server_notification_text_no_local_inet_address)
        val contentIntent = FtpServerActivity::class.createIntent()
        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }
        val contentPendingIntent = PendingIntent.getActivity(
            service, FtpServerActivity::class.hashCode(), contentIntent, pendingIntentFlags
        )
        val stopIntent = FtpServerReceiver.createIntent()
        val stopPendingIntent = PendingIntent.getBroadcast(
            service, FtpServerReceiver::class.hashCode(), stopIntent, pendingIntentFlags
        )
        val notification = ftpServerServiceNotificationTemplate.createBuilder(service)
            .setContentText(contextText)
            .setContentIntent(contentPendingIntent)
            .addAction(
                R.drawable.stop_icon_white_24dp, service.getString(R.string.stop), stopPendingIntent
            )
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(
                NotificationIds.FTP_SERVER,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            service.startForeground(NotificationIds.FTP_SERVER, notification)
        }
    }

    fun stopForeground() {
        receiver.unregister()
        service.stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}
