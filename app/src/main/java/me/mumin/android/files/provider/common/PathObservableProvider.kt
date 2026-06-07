/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java8.nio.file.Path
import java.io.IOException

interface PathObservableProvider {
    @Throws(IOException::class)
    fun observe(path: Path, intervalMillis: Long): PathObservable
}
