package me.mumin.android.files.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.commit
import me.mumin.android.files.app.AppActivity
import me.mumin.android.files.util.args
import me.mumin.android.files.util.putArgs

class EditExternalStorageShortcutDialogActivity : AppActivity() {
    private val args by args<EditExternalStorageShortcutDialogFragment.Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            val fragment = EditExternalStorageShortcutDialogFragment().putArgs(args)
            supportFragmentManager.commit {
                add(fragment, EditExternalStorageShortcutDialogFragment::class.java.name)
            }
        }
    }
}
