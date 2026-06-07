/*
 * Copyright (c) 2023 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.compat

import android.app.Service
import androidx.core.app.ServiceCompat

fun Service.stopForegroundCompat(flags: Int) {
    ServiceCompat.stopForeground(this, flags)
}
