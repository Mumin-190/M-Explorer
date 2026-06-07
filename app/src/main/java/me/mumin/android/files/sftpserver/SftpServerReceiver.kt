/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

package me.mumin.android.files.sftpserver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class SftpServerReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (val action = intent.action) {
            ACTION_STOP -> SftpServerService.stop(context)
            else -> throw IllegalArgumentException(action)
        }
    }

    companion object {
        const val ACTION_STOP = "stop"
    }
}
