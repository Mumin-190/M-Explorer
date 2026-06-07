/*
 * Copyright (c) 2024 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.webdav.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}
