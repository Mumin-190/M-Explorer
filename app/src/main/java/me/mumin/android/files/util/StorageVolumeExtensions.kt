package me.mumin.android.files.util

import android.os.storage.StorageVolume
import me.mumin.android.files.compat.directoryCompat

val StorageVolume.isMounted: Boolean
    get() = directoryCompat != null
