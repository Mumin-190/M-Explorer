/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java8.nio.file.DirectoryStream
import java8.nio.file.Path

class PathListDirectoryStream(
    paths: List<Path>,
    filter: DirectoryStream.Filter<in Path>
) : PathIteratorDirectoryStream(paths.iterator(), null, filter)
