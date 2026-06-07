/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.fileproperties.permission

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import java8.nio.file.Path
import me.mumin.android.files.R
import me.mumin.android.files.file.FileItem
import me.mumin.android.files.filejob.FileJobService
import me.mumin.android.files.provider.common.PosixFileAttributes
import me.mumin.android.files.provider.common.PosixGroup
import me.mumin.android.files.provider.common.toByteString
import me.mumin.android.files.util.SelectionLiveData
import me.mumin.android.files.util.putArgs
import me.mumin.android.files.util.show
import me.mumin.android.files.util.viewModels

class SetGroupDialogFragment : SetPrincipalDialogFragment() {
    override val viewModel: SetPrincipalViewModel by viewModels { { SetGroupViewModel() } }

    @StringRes
    override val titleRes: Int = R.string.file_properties_permission_set_group_title

    override fun createAdapter(selectionLiveData: SelectionLiveData<Int>): PrincipalListAdapter =
        GroupListAdapter(selectionLiveData)

    override val PosixFileAttributes.principal
        get() = group()!!

    override fun setPrincipal(path: Path, principal: PrincipalItem, recursive: Boolean) {
        val group = PosixGroup(principal.id, principal.name?.toByteString())
        FileJobService.setGroup(path, group, recursive, requireContext())
    }

    companion object {
        fun show(file: FileItem, fragment: Fragment) {
            SetGroupDialogFragment().putArgs(Args(file)).show(fragment)
        }
    }
}
