/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.compat

import kotlin.comparisons.reversed as kotlinReversed
import kotlin.collections.removeFirst as kotlinRemoveFirst
import kotlin.collections.removeLast as kotlinRemoveLast

fun <T> Comparator<T>.reversedCompat(): Comparator<T> = kotlinReversed()

fun <T> MutableList<T>.removeFirstCompat(): T = kotlinRemoveFirst()

fun <T> MutableList<T>.removeLastCompat(): T = kotlinRemoveLast()
