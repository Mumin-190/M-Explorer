/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.util

fun Any.hash(vararg values: Any?): Int = values.contentDeepHashCode()
