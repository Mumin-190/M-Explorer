/*
 * Copyright (c) 2024 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java.nio.ByteBuffer
import kotlin.reflect.KClass

private val EMPTY_BYTE_BUFFER = ByteBuffer.allocate(0)

val KClass<ByteBuffer>.EMPTY: ByteBuffer
    get() = EMPTY_BYTE_BUFFER
