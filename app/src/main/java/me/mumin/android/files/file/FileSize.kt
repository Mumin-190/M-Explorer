/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.file

import android.content.Context
import android.text.format.Formatter
import me.mumin.android.files.R
import me.mumin.android.files.util.getQuantityString

@JvmInline
value class FileSize(val value: Long) {

    /* @see android.text.format.Formatter#formatBytes(Resources, long, int) */
    val isHumanReadableInBytes: Boolean
        get() = value <= 900

    fun formatInBytes(context: Context): String =
        context.getQuantityString(R.plurals.size_in_bytes_format, value.toInt(), value)

    fun formatHumanReadable(context: Context): String =
        Formatter.formatFileSize(context, value)
}

fun Long.asFileSize(): FileSize = FileSize(this)
