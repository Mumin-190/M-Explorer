package me.mumin.android.files.provider.root

import me.mumin.android.files.provider.common.PosixFileStore
import me.mumin.android.files.provider.remote.RemoteInterface
import me.mumin.android.files.provider.remote.RemotePosixFileStore

class RootPosixFileStore(fileStore: PosixFileStore) : RemotePosixFileStore(
    RemoteInterface { RootFileService.getRemotePosixFileStoreInterface(fileStore) }
)
