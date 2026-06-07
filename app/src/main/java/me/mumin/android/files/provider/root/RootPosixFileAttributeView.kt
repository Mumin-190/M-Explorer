package me.mumin.android.files.provider.root

import me.mumin.android.files.provider.common.PosixFileAttributeView
import me.mumin.android.files.provider.remote.RemoteInterface
import me.mumin.android.files.provider.remote.RemotePosixFileAttributeView

open class RootPosixFileAttributeView(
    attributeView: PosixFileAttributeView
) : RemotePosixFileAttributeView(
    RemoteInterface { RootFileService.getRemotePosixFileAttributeViewInterface(attributeView) }
) {
    override fun name(): String {
        throw AssertionError()
    }
}
