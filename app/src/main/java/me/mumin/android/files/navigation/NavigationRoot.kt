/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.navigation

import android.content.Context
import java8.nio.file.Path

interface NavigationRoot {
    val path: Path

    fun getName(context: Context): String
}
