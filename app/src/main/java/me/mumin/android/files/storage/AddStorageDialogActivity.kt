package me.mumin.android.files.storage

import android.os.Bundle
import android.view.View
import androidx.fragment.app.add
import androidx.fragment.app.commit
import me.mumin.android.files.app.AppActivity

class AddStorageDialogActivity : AppActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Calls ensureSubDecor().
        findViewById<View>(android.R.id.content)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add<AddStorageDialogFragment>(AddStorageDialogFragment::class.java.name)
            }
        }
    }
}
