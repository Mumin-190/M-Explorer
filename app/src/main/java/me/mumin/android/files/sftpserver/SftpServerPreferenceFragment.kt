/*
 * Copyright (c) 2026 Mumin-190
 * All Rights Reserved.
 */

package me.mumin.android.files.sftpserver

import android.os.Bundle
import me.mumin.android.files.R
import me.mumin.android.files.ui.PreferenceFragmentCompat

class SftpServerPreferenceFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferencesFix(savedInstanceState: Bundle?, rootKey: String?) {
        addPreferencesFromResource(R.xml.sftp_server)
    }
}
