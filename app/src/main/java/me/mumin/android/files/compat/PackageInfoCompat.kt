/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.compat

import android.content.pm.PackageInfo
import androidx.core.content.pm.PackageInfoCompat

val PackageInfo.longVersionCodeCompat: Long
    get() = PackageInfoCompat.getLongVersionCode(this)
