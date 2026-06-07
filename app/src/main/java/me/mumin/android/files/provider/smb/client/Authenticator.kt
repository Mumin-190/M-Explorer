package me.mumin.android.files.provider.smb.client

interface Authenticator {
    fun getPassword(authority: Authority): String?
}
