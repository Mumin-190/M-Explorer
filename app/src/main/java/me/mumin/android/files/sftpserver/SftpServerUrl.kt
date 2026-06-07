/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

package me.mumin.android.files.sftpserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.util.RuntimeBroadcastReceiver
import me.mumin.android.files.util.getLocalAddress
import me.mumin.android.files.util.valueCompat
import java.net.InetAddress

object SftpServerUrl {
    fun getUrl(): String? {
        val localAddress = InetAddress::class.getLocalAddress() ?: return null
        val host = localAddress.hostAddress
        val port = Settings.SFTP_SERVER_PORT.valueCompat
        return "sftp://$host:$port/"
    }

    fun createChangeReceiver(context: Context, onChange: () -> Unit): RuntimeBroadcastReceiver =
        RuntimeBroadcastReceiver(
            IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION), object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    onChange()
                }
            }, context
        )
}
