/*
 * Copyright (c) 2021 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.util

import kotlinx.coroutines.flow.MutableStateFlow

@Suppress("FunctionName", "UNCHECKED_CAST")
fun <T : Any> LateInitMutableStateFlow(): MutableStateFlow<T> =
    MutableStateFlow<T?>(null) as MutableStateFlow<T>
