/*
 * Copyright (c) 2019 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.RecyclerView

class SelectionLiveData<Key> : MutableLiveData<Key>() {
    fun observe(owner: LifecycleOwner, adapter: RecyclerView.Adapter<*>) {
        observe(owner) {
            adapter.notifyItemRangeChanged(0, adapter.itemCount, PAYLOAD_SELECTION_CHANGED)
        }
    }

    companion object {
        val PAYLOAD_SELECTION_CHANGED = Any()
    }
}
