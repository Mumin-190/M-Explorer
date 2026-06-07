/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.filelist

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import me.mumin.android.files.R
import me.mumin.android.files.util.show

class CreateDirectoryDialogFragment : FileNameDialogFragment() {
    override val listener: Listener
        get() = super.listener as Listener

    @StringRes
    override val titleRes: Int = R.string.file_create_directory_title

    override fun onOk(name: String) {
        listener.createDirectory(name)
    }

    companion object {
        fun show(fragment: Fragment) {
            CreateDirectoryDialogFragment().show(fragment)
        }
    }

    interface Listener : FileNameDialogFragment.Listener {
        fun createDirectory(name: String)
    }
}
