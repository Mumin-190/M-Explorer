/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java.io.IOException

abstract class PosixFileStore : AbstractFileStore() {
    @Throws(IOException::class)
    abstract fun refresh()

    @Throws(IOException::class)
    abstract fun setReadOnly(readOnly: Boolean)
}
