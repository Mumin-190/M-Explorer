/*
 * Copyright (c) 2021 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.sftp.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}
