/*
 * Copyright (c) 2022 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.hiddenapi

import android.os.Build

object HiddenApi {
    fun disableHiddenApiChecks() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            System.loadLibrary("hiddenapi")
        }
    }
}
