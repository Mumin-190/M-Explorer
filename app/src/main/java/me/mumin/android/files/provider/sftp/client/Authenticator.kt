package me.mumin.android.files.provider.sftp.client

interface Authenticator {
    fun getAuthentication(authority: Authority): Authentication?
}
