/*
 * Copyright (c) 2023 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.common

import java8.nio.file.attribute.BasicFileAttributes

interface EncryptedFileAttributes {
    fun isEncrypted(): Boolean
}

fun BasicFileAttributes.isEncrypted(): Boolean =
    if (this is EncryptedFileAttributes) isEncrypted() else false
