package me.mumin.android.files.storage

import me.mumin.android.files.provider.webdav.client.Authentication
import me.mumin.android.files.provider.webdav.client.Authenticator
import me.mumin.android.files.provider.webdav.client.Authority
import me.mumin.android.files.settings.Settings
import me.mumin.android.files.util.valueCompat

object WebDavServerAuthenticator : Authenticator {
    private val transientServers = mutableSetOf<WebDavServer>()

    override fun getAuthentication(authority: Authority): Authentication? {
        val server = synchronized(transientServers) {
            transientServers.find { it.authority == authority }
        } ?: Settings.STORAGES.valueCompat.find {
            it is WebDavServer && it.authority == authority
        } as WebDavServer?
        return server?.authentication
    }

    fun addTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers += server }
    }

    fun removeTransientServer(server: WebDavServer) {
        synchronized(transientServers) { transientServers -= server }
    }
}
