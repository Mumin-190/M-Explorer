/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.fileproperties.permission

import android.os.Bundle
import me.mumin.android.files.R
import me.mumin.android.files.file.FileItem
import me.mumin.android.files.fileproperties.FilePropertiesFileViewModel
import me.mumin.android.files.fileproperties.FilePropertiesTabFragment
import me.mumin.android.files.provider.common.PosixFileAttributes
import me.mumin.android.files.provider.common.PosixPrincipal
import me.mumin.android.files.provider.common.toInt
import me.mumin.android.files.provider.common.toModeString
import me.mumin.android.files.util.Stateful
import me.mumin.android.files.util.viewModels

class FilePropertiesPermissionTabFragment : FilePropertiesTabFragment() {
    private val viewModel by viewModels<FilePropertiesFileViewModel>({ requireParentFragment() })

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        viewModel.fileLiveData.observe(viewLifecycleOwner) { onFileChanged(it) }
    }

    override fun refresh() {
        viewModel.reload()
    }

    private fun onFileChanged(stateful: Stateful<FileItem>) {
        bindView(stateful) { file ->
            val attributes = file.attributes as PosixFileAttributes
            val owner = attributes.owner()
            addItemView(
                R.string.file_properties_permission_owner, getPrincipalText(owner), owner?.let {
                    { SetOwnerDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                }
            )
            val group = attributes.group()
            addItemView(
                R.string.file_properties_permission_group, getPrincipalText(group), group?.let {
                    { SetGroupDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                }
            )
            val mode = attributes.mode()
            addItemView(
                R.string.file_properties_permission_mode, if (mode != null) {
                    getString(
                        R.string.file_properties_permission_mode_format, mode.toModeString(),
                        mode.toInt()
                    )
                } else {
                    getString(R.string.unknown)
                }, if (mode != null && !attributes.isSymbolicLink) {
                    { SetModeDialogFragment.show(file, this@FilePropertiesPermissionTabFragment) }
                } else {
                    null
                }
            )
            val seLinuxContext = attributes.seLinuxContext()
            if (seLinuxContext != null) {
                addItemView(
                    R.string.file_properties_permission_selinux_context,
                    if (seLinuxContext.isNotEmpty()) {
                        seLinuxContext.toString()
                    } else {
                        getString(R.string.empty_placeholder)
                    }
                ) {
                    SetSeLinuxContextDialogFragment.show(
                        file, this@FilePropertiesPermissionTabFragment
                    )
                }
            }
        }
    }

    private fun getPrincipalText(principal: PosixPrincipal?) =
        if (principal != null) {
            if (principal.name != null) {
                getString(
                    R.string.file_properties_permission_principal_format, principal.name,
                    principal.id
                )
            } else {
                principal.id.toString()
            }
        } else {
            getString(R.string.unknown)
        }

    companion object {
        fun isAvailable(file: FileItem): Boolean {
            val attributes = file.attributes
            return attributes is PosixFileAttributes && (attributes.owner() != null
                || attributes.group() != null || attributes.mode() != null
                || attributes.seLinuxContext() != null)
        }
    }
}
