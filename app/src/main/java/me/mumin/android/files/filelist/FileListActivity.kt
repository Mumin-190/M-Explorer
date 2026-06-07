/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.filelist

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.activity.result.contract.ActivityResultContract
import androidx.fragment.app.commit
import java8.nio.file.Path
import me.mumin.android.files.R
import me.mumin.android.files.app.AppActivity
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.util.createIntent
import me.mumin.android.files.util.extraPath
import me.mumin.android.files.util.putArgs

import android.os.Environment
import java8.nio.file.Paths
import androidx.core.os.BundleCompat
import me.mumin.android.files.sftpserver.ServerFragment

class FileListActivity : AppActivity() {

    private var pendingPath: Path? = null
    private lateinit var backCallback: androidx.activity.OnBackPressedCallback
    private lateinit var bottomNavigation: com.google.android.material.bottomnavigation.BottomNavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        bottomNavigation = findViewById(R.id.bottom_navigation)
        
        backCallback = object : androidx.activity.OnBackPressedCallback(false) {
            override fun handleOnBackPressed() {
                bottomNavigation.selectedItemId = R.id.tab_home
            }
        }
        onBackPressedDispatcher.addCallback(this, backCallback)

        bottomNavigation.setOnItemSelectedListener { item ->
            val fragment = when (item.itemId) {
                R.id.tab_home -> HomeFragment()
                R.id.tab_browse -> {
                    val extPath = Environment.getExternalStorageDirectory().absolutePath
                    val path = pendingPath ?: Paths.get(extPath)
                    pendingPath = null
                    val intent = Intent().apply {
                        extraPath = path
                    }
                    FileListFragment().putArgs(FileListFragment.Args(intent))
                }
                R.id.tab_server -> ServerFragment()
                else -> return@setOnItemSelectedListener false
            }
            backCallback.isEnabled = item.itemId != R.id.tab_home
            supportFragmentManager.popBackStack(null, androidx.fragment.app.FragmentManager.POP_BACK_STACK_INCLUSIVE)
            supportFragmentManager.commit {
                replace(R.id.fragment_container, fragment)
            }
            true
        }

        if (savedInstanceState == null) {
            val path = intent.extraPath
            if (path != null && intent.action != Intent.ACTION_MAIN) {
                pendingPath = path
                bottomNavigation.selectedItemId = R.id.tab_browse
            } else {
                bottomNavigation.selectedItemId = R.id.tab_home
            }
        } else {
            backCallback.isEnabled = bottomNavigation.selectedItemId != R.id.tab_home
        }
    }

    fun navigateToPath(path: Path, intent: Intent = Intent()) {
        val launchIntent = Intent(intent).apply {
            extraPath = path
        }
        val fragment = FileListFragment().putArgs(FileListFragment.Args(launchIntent))
        supportFragmentManager.commit {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
        }
    }

    override fun onKeyShortcut(keyCode: Int, event: KeyEvent): Boolean {
        val currentFragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (currentFragment is FileListFragment && currentFragment.onKeyShortcut(keyCode, event)) {
            return true
        }
        return super.onKeyUp(keyCode, event)
    }

    companion object {
        fun createViewIntent(path: Path): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_VIEW)
                .apply { extraPath = path }
    }

    class OpenFileContract : ActivityResultContract<List<MimeType>, Path?>() {
        override fun createIntent(context: Context, input: List<MimeType>): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT)
                .setType(MimeType.ANY.value)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .putExtra(Intent.EXTRA_MIME_TYPES, input.map { it.value }.toTypedArray())

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }

    class CreateFileContract : ActivityResultContract<Triple<MimeType, String?, Path?>, Path?>() {
        override fun createIntent(
            context: Context,
            input: Triple<MimeType, String?, Path?>
        ): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_CREATE_DOCUMENT)
                .setType(input.first.value)
                .addCategory(Intent.CATEGORY_OPENABLE)
                .apply {
                    input.second?.let { putExtra(Intent.EXTRA_TITLE, it) }
                    input.third?.let { extraPath = it }
                }

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }

    class OpenDirectoryContract : ActivityResultContract<Path?, Path?>() {
        override fun createIntent(context: Context, input: Path?): Intent =
            FileListActivity::class.createIntent()
                .setAction(Intent.ACTION_OPEN_DOCUMENT_TREE)
                .apply { input?.let { extraPath = it } }

        override fun parseResult(resultCode: Int, intent: Intent?): Path? =
            if (resultCode == RESULT_OK) intent?.extraPath else null
    }
}
