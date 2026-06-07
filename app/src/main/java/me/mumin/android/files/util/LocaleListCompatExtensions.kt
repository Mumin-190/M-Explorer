/*
 * Copyright (c) 2023 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.util

import androidx.core.os.LocaleListCompat
import java.util.Locale

fun LocaleListCompat.toList(): List<Locale> = List(size()) { this[it]!! }
