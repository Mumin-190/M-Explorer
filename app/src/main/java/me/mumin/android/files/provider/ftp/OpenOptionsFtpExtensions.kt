/*
 * Copyright (c) 2022 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.ftp

import java8.nio.file.StandardOpenOption
import me.mumin.android.files.provider.common.OpenOptions

internal fun OpenOptions.checkForFtp() {
    if (deleteOnClose) {
        throw UnsupportedOperationException(StandardOpenOption.DELETE_ON_CLOSE.toString())
    }
    if (sync) {
        throw UnsupportedOperationException(StandardOpenOption.SYNC.toString())
    }
    if (dsync) {
        throw UnsupportedOperationException(StandardOpenOption.DSYNC.toString())
    }
}
