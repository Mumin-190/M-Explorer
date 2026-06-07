/*
 * Copyright (c) 2022 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.ftp.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}
