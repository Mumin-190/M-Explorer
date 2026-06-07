/*
 * Copyright (c) 2018 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.provider.linux

import android.os.Parcel
import android.os.Parcelable
import me.mumin.android.files.provider.root.RootPosixFileStore
import me.mumin.android.files.provider.root.RootablePosixFileStore
import me.mumin.android.files.util.readParcelable

internal class LinuxFileStore private constructor(
    private val path: LinuxPath,
    private val localFileStore: LocalLinuxFileStore
) : RootablePosixFileStore(path, localFileStore, { RootPosixFileStore(it) }) {
    constructor(path: LinuxPath) : this(path, LocalLinuxFileStore(path))

    private constructor(source: Parcel) : this(source.readParcelable()!!, source.readParcelable()!!)

    override fun describeContents(): Int = 0

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeParcelable(path, flags)
        dest.writeParcelable(localFileStore, flags)
    }

    companion object {
        @JvmField
        val CREATOR = object : Parcelable.Creator<LinuxFileStore> {
            override fun createFromParcel(source: Parcel): LinuxFileStore = LinuxFileStore(source)

            override fun newArray(size: Int): Array<LinuxFileStore?> = arrayOfNulls(size)
        }
    }
}
