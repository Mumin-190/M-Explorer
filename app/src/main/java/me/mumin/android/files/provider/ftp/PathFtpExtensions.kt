/*
 * Copyright (c) 2022 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.ftp

import java8.nio.file.Path
import me.mumin.android.files.provider.ftp.client.Authority

fun Authority.createFtpRootPath(): Path =
    FtpFileSystemProvider.getOrNewFileSystem(this).rootDirectory
