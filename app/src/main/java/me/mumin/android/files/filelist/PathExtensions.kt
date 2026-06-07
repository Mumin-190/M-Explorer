/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.filelist

import java8.nio.file.Path
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.isSupportedArchive
import me.mumin.android.files.provider.archive.archiveFile
import me.mumin.android.files.provider.archive.isArchivePath
import me.mumin.android.files.provider.document.isDocumentPath
import me.mumin.android.files.provider.document.resolver.DocumentResolver
import me.mumin.android.files.provider.linux.isLinuxPath

val Path.name: String
    get() = fileName?.toString() ?: if (isArchivePath) archiveFile.fileName.toString() else "/"

fun Path.toUserFriendlyString(): String = if (isLinuxPath) toFile().path else toUri().toString()

fun Path.isArchiveFile(mimeType: MimeType): Boolean = !isArchivePath && mimeType.isSupportedArchive

val Path.isLocalPath: Boolean
    get() =
        isLinuxPath || (isDocumentPath && DocumentResolver.isLocal(this as DocumentResolver.Path))

val Path.isRemotePath: Boolean
    get() = !isLocalPath
