package me.mumin.android.files.provider.sftp.client

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import net.schmizz.sshj.DefaultConfig
import net.schmizz.sshj.common.Factory
import net.schmizz.sshj.userauth.keyprovider.KeyProvider
import net.schmizz.sshj.userauth.keyprovider.KeyProviderUtil
import net.schmizz.sshj.userauth.method.AuthMethod
import net.schmizz.sshj.userauth.method.AuthPassword
import net.schmizz.sshj.userauth.method.AuthPublickey
import net.schmizz.sshj.userauth.password.PasswordUtils
import java.io.IOException

sealed class Authentication : Parcelable {
    abstract fun toAuthMethod(): AuthMethod
}

@Parcelize
data class PasswordAuthentication(
    val id: String
) : Authentication() {
    val password: String
        get() = if (isId(id)) {
            SftpSecurity.encryptedPreferences.getString(id, "") ?: ""
        } else {
            id // legacy data
        }

    override fun toAuthMethod(): AuthMethod =
        AuthPassword(PasswordUtils.createOneOff(password.toCharArray()))

    companion object {
        fun isId(str: String): Boolean = str.length == 36 && str.count { it == '-' } == 4
        
        fun createNew(password: String): PasswordAuthentication {
            val newId = java.util.UUID.randomUUID().toString()
            SftpSecurity.encryptedPreferences.edit().putString(newId, password).apply()
            return PasswordAuthentication(newId)
        }
    }
}

@Parcelize
data class PublicKeyAuthentication(
    val id: String,
    val legacyPassword: String?
) : Authentication() {
    val privateKey: String
        get() = if (PasswordAuthentication.isId(id)) {
            SftpSecurity.encryptedPreferences.getString("${id}_key", "") ?: ""
        } else {
            id // legacy data
        }

    val privateKeyPassword: String?
        get() = if (PasswordAuthentication.isId(id)) {
            SftpSecurity.encryptedPreferences.getString("${id}_pass", null)
        } else {
            legacyPassword
        }

    override fun toAuthMethod(): AuthMethod =
        AuthPublickey(createKeyProvider(privateKey, privateKeyPassword))

    companion object {
        private val KEY_PROVIDER_FACTORIES = DefaultConfig().fileKeyProviderFactories

        fun createNew(privateKey: String, privateKeyPassword: String?): PublicKeyAuthentication {
            val newId = java.util.UUID.randomUUID().toString()
            SftpSecurity.encryptedPreferences.edit()
                .putString("${newId}_key", privateKey)
                .putString("${newId}_pass", privateKeyPassword)
                .apply()
            return PublicKeyAuthentication(newId, null)
        }

        fun validate(privateKey: String, privateKeyPassword: String?): IOException? =
            try {
                createKeyProvider(privateKey, privateKeyPassword).private
                null
            } catch (e: IOException) {
                e
            }

        @Throws(IOException::class)
        private fun createKeyProvider(
            privateKey: String,
            privateKeyPassword: String?
        ): KeyProvider {
            val format = KeyProviderUtil.detectKeyFileFormat(privateKey, false)
            val keyProvider = Factory.Named.Util.create(KEY_PROVIDER_FACTORIES, format.toString())
                ?: throw IOException("No key provider factory found for $format")
            keyProvider.init(
                privateKey, null,
                privateKeyPassword?.let { PasswordUtils.createOneOff(it.toCharArray()) }
            )
            return keyProvider
        }
    }
}
