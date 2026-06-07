/*
 * Copyright (c) 2021 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.filelist

import android.os.Bundle
import java8.nio.file.Path
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.WriteWith
import me.mumin.android.files.app.AppActivity
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.fileProviderUri
import me.mumin.android.files.util.ParcelableArgs
import me.mumin.android.files.util.ParcelableParceler
import me.mumin.android.files.util.args
import me.mumin.android.files.util.createEditIntent
import me.mumin.android.files.util.startActivitySafe

// Use a trampoline activity so that we can have a proper icon and title.
class EditFileActivity : AppActivity() {
    private val args by args<Args>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        startActivitySafe(args.path.fileProviderUri.createEditIntent(args.mimeType))
        finish()
    }

    @Parcelize
    class Args(
        val path: @WriteWith<ParcelableParceler> Path,
        val mimeType: MimeType
    ) : ParcelableArgs
}
