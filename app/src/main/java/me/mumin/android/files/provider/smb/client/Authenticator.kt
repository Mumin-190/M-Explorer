/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.smb.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}
