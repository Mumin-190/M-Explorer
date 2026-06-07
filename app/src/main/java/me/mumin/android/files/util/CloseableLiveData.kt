/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.util

import androidx.lifecycle.LiveData
import java.io.Closeable

abstract class CloseableLiveData<T> : LiveData<T>, Closeable {
    constructor(value: T) : super(value)

    constructor()

    abstract override fun close()
}
