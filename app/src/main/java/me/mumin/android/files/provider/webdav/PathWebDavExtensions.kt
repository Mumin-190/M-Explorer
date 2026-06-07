/*
 * Copyright (c) 2024 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.webdav

import java8.nio.file.Path
import me.mumin.android.files.provider.webdav.client.Authority

fun Authority.createWebDavRootPath(): Path =
    WebDavFileSystemProvider.getOrNewFileSystem(this).rootDirectory
