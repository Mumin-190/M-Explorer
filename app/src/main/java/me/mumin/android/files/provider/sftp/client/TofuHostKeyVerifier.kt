package me.mumin.android.files.provider.sftp.client

import android.content.SharedPreferences
import android.util.Log
import net.schmizz.sshj.common.SecurityUtils
import net.schmizz.sshj.transport.verification.HostKeyVerifier
import java.security.PublicKey

class TofuHostKeyVerifier(private val prefs: SharedPreferences) : HostKeyVerifier {
    override fun verify(hostname: String, port: Int, key: PublicKey): Boolean {
        val fingerprint = SecurityUtils.getFingerprint(key)
        val prefKey = "hostkey_${hostname}_${port}"
        val existing = prefs.getString(prefKey, null)

        if (existing == null) {
            Log.i("SFTP", "Storing new host key for $hostname:$port -> $fingerprint")
            prefs.edit().putString(prefKey, fingerprint).apply()
            return true
        } else if (existing == fingerprint) {
            return true
        } else {
            Log.e("SFTP", "Host key changed for $hostname:$port! Expected $existing, got $fingerprint")
            return false
        }
    }

    override fun findExistingAlgorithms(hostname: String, port: Int): List<String> {
        return emptyList()
    }
}
