package me.mumin.android.files.file

import java.time.Instant
import java8.nio.file.attribute.BasicFileAttributes

val BasicFileAttributes.fileSize: FileSize
    get() = size().asFileSize()

val BasicFileAttributes.lastModifiedInstant: Instant
    get() = lastModifiedTime().toInstant()
