package me.mumin.android.files.provider.remote;

import me.mumin.android.files.provider.remote.ParcelableException;

interface IRemoteFileSystem {
    void close(out ParcelableException exception);
}
