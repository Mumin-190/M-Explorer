package me.mumin.android.files.fileproperties.permission

import androidx.annotation.DrawableRes
import me.mumin.android.files.R
import me.mumin.android.files.util.SelectionLiveData

class UserListAdapter(
    selectionLiveData: SelectionLiveData<Int>
) : PrincipalListAdapter(selectionLiveData) {
    @DrawableRes
    override val principalIconRes: Int = R.drawable.person_icon_control_normal_24dp
}
