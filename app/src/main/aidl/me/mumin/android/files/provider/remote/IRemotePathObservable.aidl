package me.mumin.android.files.provider.remote;

import me.mumin.android.files.provider.remote.ParcelableException;
import me.mumin.android.files.util.RemoteCallback;

interface IRemotePathObservable {
    void addObserver(in RemoteCallback observer);

    void close(out ParcelableException exception);
}
