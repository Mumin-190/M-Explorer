/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.smb

import java8.nio.file.Path
import me.mumin.android.files.provider.smb.client.Authority

fun Authority.createSmbRootPath(): Path =
    SmbFileSystemProvider.getOrNewFileSystem(this).rootDirectory
