/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.compat

import android.content.Context

object PreferenceManagerCompat {
    fun getDefaultSharedPreferencesName(context: Context): String =
        "${context.packageName}_preferences"

    val defaultSharedPreferencesMode: Int
        get() = Context.MODE_PRIVATE
}
