/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

package me.mumin.android.files.sftpserver

import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import me.mumin.android.files.R
import me.mumin.android.files.app.NotificationIds
import me.mumin.android.files.compat.stopForegroundCompat
import me.mumin.android.files.util.NotificationChannelTemplate
import me.mumin.android.files.util.NotificationTemplate

val sftpServerServiceNotificationTemplate =
    NotificationTemplate(
        NotificationChannelTemplate(
            "sftp_server",
            R.string.notification_channel_sftp_server_name,
            NotificationManagerCompat.IMPORTANCE_LOW,
            descriptionRes = R.string.notification_channel_sftp_server_description,
            showBadge = false
        ),
        colorRes = R.color.color_primary,
        smallIcon = R.drawable.notification_icon,
        contentTitleRes = R.string.sftp_server_notification_title,
        ongoing = true,
        onlyAlertOnce = true,
        category = NotificationCompat.CATEGORY_SERVICE,
        priority = NotificationCompat.PRIORITY_LOW
    )

class SftpServerNotification(private val service: Service) {
    private val receiver = SftpServerUrl.createChangeReceiver(service) { doStartForeground() }

    fun startForeground() {
        doStartForeground()
        receiver.register()
    }

    private fun doStartForeground() {
        val contextText = SftpServerUrl.getUrl()
            ?: service.getString(R.string.ftp_server_notification_text_no_local_inet_address)
        var pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            pendingIntentFlags = pendingIntentFlags or PendingIntent.FLAG_IMMUTABLE
        }

        // Action intent to stop the service
        val stopIntent = Intent(service, SftpServerReceiver::class.java).apply {
            action = SftpServerReceiver.ACTION_STOP
        }
        val stopPendingIntent = PendingIntent.getBroadcast(
            service, SftpServerReceiver::class.hashCode(), stopIntent, pendingIntentFlags
        )

        val notification = sftpServerServiceNotificationTemplate.createBuilder(service)
            .setContentText(contextText)
            .addAction(
                R.drawable.stop_icon_white_24dp, service.getString(R.string.stop), stopPendingIntent
            )
            .build()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            service.startForeground(
                NotificationIds.SFTP_SERVER,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            service.startForeground(NotificationIds.SFTP_SERVER, notification)
        }
    }

    fun stopForeground() {
        receiver.unregister()
        service.stopForegroundCompat(ServiceCompat.STOP_FOREGROUND_REMOVE)
    }
}
