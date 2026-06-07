package me.mumin.android.files.provider.webdav.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}
