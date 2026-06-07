package me.mumin.android.files.provider.remote;

import me.mumin.android.files.provider.remote.IRemoteFileSystem;
import me.mumin.android.files.provider.remote.IRemoteFileSystemProvider;
import me.mumin.android.files.provider.remote.IRemotePosixFileAttributeView;
import me.mumin.android.files.provider.remote.IRemotePosixFileStore;
import me.mumin.android.files.provider.remote.ParcelableObject;

interface IRemoteFileService {
    IRemoteFileSystemProvider getRemoteFileSystemProviderInterface(String scheme);

    IRemoteFileSystem getRemoteFileSystemInterface(in ParcelableObject fileSystem);

    IRemotePosixFileStore getRemotePosixFileStoreInterface(in ParcelableObject fileStore);

    IRemotePosixFileAttributeView getRemotePosixFileAttributeViewInterface(
        in ParcelableObject attributeView
    );
}
