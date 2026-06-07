/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.filelist

import android.content.Context
import java8.nio.file.Path

data class BreadcrumbData(
    val paths: List<Path>,
    val nameProducers: List<(Context) -> String>,
    val selectedIndex: Int
)
