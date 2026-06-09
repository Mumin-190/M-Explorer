package me.mumin.android.files.storage

import android.content.Context
import android.view.ViewGroup
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange
import com.h6ah4i.android.widget.advrecyclerview.utils.AbstractDraggableItemViewHolder
import me.mumin.android.files.compat.foregroundCompat
import me.mumin.android.files.compat.isTransformedTouchPointInViewCompat
import me.mumin.android.files.databinding.StorageItemBinding
import me.mumin.android.files.ui.SimpleAdapter
import me.mumin.android.files.util.layoutInflater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.lifecycle.lifecycleScope
import me.mumin.android.files.provider.common.newDirectoryStream

class StorageListAdapter(
    private val lifecycleOwner: androidx.lifecycle.LifecycleOwner,
    private val listener: Listener
) : SimpleAdapter<Storage, StorageListAdapter.ViewHolder>(),
    DraggableItemAdapter<StorageListAdapter.ViewHolder> {
    override val hasStableIds: Boolean
        get() = true

    override fun getItemId(position: Int): Long = getItem(position).id

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder =
        ViewHolder(
            StorageItemBinding.inflate(parent.context.layoutInflater, parent, false)
        )

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val storage = getItem(position)
        val binding = holder.binding
        // Need to remove the ripple before it's drawn onto the bitmap for dragging.
        binding.root.foregroundCompat!!.mutate().setVisible(!holder.dragState.isActive, false)
        binding.root.setOnClickListener {
            val ctx = binding.statusText.context
            lifecycleOwner.lifecycleScope.launch {
                try {
                    ServerStatusManager.checkServerStatus(ctx, storage, force = true)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            listener.editStorage(storage)
        }
        binding.iconImage.setImageResource(storage.iconRes)
        binding.nameText.isActivated = storage.isVisible
        binding.nameText.text = storage.getName(binding.nameText.context)
        binding.descriptionText.text = storage.description

        // Dynamic availability status check
        val isServer = storage is FtpServer 
            || storage is SftpServer
            || storage is SmbServer
            || storage is WebDavServer

        if (isServer) {
            val serverId = storage.id.toString()
            
            fun updateAdapterUI(status: String, lastSuccessTime: Long) {
                if (status == "Offline" || status == "Connecting") {
                    val ctx = binding.statusText.context
                    val lastSeenTime = if (lastSuccessTime > 0L) lastSuccessTime else {
                        ctx.getSharedPreferences("server_last_seen", Context.MODE_PRIVATE).getLong(serverId, 0L) ?: 0L
                    }
                    if (lastSeenTime > 0L) {
                        val diffMs = System.currentTimeMillis() - lastSeenTime
                        val diffMinutes = diffMs / (1000 * 60)
                        val diffHours = diffMinutes / 60
                        val diffDays = diffHours / 24
                        val lastSeenStr = when {
                            diffDays > 0 -> "Last seen ${diffDays}d ago"
                            diffHours > 0 -> "Last seen ${diffHours}h ago"
                            diffMinutes > 0 -> "Last seen ${diffMinutes}m ago"
                            else -> "Last seen just now"
                        }
                        binding.statusText.text = if (status == "Connecting") "Connecting… ($lastSeenStr)" else lastSeenStr
                    } else {
                        binding.statusText.text = status
                    }
                } else {
                    binding.statusText.text = status
                }
            }

            val cached = ServerStatusManager.getCachedStatus(serverId)
            if (cached != null) {
                updateAdapterUI(cached.status, cached.lastSuccessfulConnection)
            } else {
                updateAdapterUI("Connecting", 0L)
            }

            lifecycleOwner.lifecycleScope.launch {
                val ctx = binding.statusText.context
                val info = ServerStatusManager.checkServerStatus(ctx, storage, force = false)
                updateAdapterUI(info.status, info.lastSuccessfulConnection)
            }
        } else {
            val isAvailable = try {
                val f = java.io.File(storage.linuxPath ?: "/")
                f.exists()
            } catch (e: Exception) {
                false
            }
            binding.statusText.text = if (isAvailable) "Available" else "Unavailable"
        }
    }

    override fun onCheckCanStartDrag(holder: ViewHolder, position: Int, x: Int, y: Int): Boolean =
        (holder.binding.root as ViewGroup).isTransformedTouchPointInViewCompat(
            x.toFloat(), y.toFloat(), holder.binding.dragHandleView, null
        )

    override fun onGetItemDraggableRange(holder: ViewHolder, position: Int): ItemDraggableRange? =
        null

    override fun onCheckCanDrop(draggingPosition: Int, dropPosition: Int): Boolean = true

    override fun onItemDragStarted(position: Int) {
        notifyDataSetChanged()
    }

    override fun onItemDragFinished(fromPosition: Int, toPosition: Int, result: Boolean) {
        notifyDataSetChanged()
    }

    override fun onMoveItem(fromPosition: Int, toPosition: Int) {
        if (fromPosition == toPosition) {
            return
        }
        listener.moveStorage(fromPosition, toPosition)
    }

    class ViewHolder(val binding: StorageItemBinding) : AbstractDraggableItemViewHolder(
        binding.root
    )

    interface Listener {
        fun editStorage(storage: Storage)
        fun moveStorage(fromPosition: Int, toPosition: Int)
    }
}
