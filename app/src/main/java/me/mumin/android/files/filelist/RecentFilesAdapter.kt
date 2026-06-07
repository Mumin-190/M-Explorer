package me.mumin.android.files.filelist

import android.content.Context
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java8.nio.file.Paths
import me.mumin.android.files.R
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.asMimeTypeOrNull
import me.mumin.android.files.file.icon
import me.mumin.android.files.file.asFileSize

data class RecentFile(
    val id: Long,
    val name: String,
    val path: String,
    val size: Long,
    val dateModified: Long,
    val uri: Uri
)

class RecentFilesAdapter(
    private val context: Context,
    private val files: List<RecentFile>,
    private val onClick: (RecentFile, MimeType) -> Unit
) : RecyclerView.Adapter<RecentFilesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val card: View = view.findViewById(R.id.recent_card)
        val icon: ImageView = view.findViewById(R.id.recent_icon)
        val name: TextView = view.findViewById(R.id.recent_name)
        val size: TextView = view.findViewById(R.id.recent_size)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.recent_file_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.name.text = file.name
        holder.size.text = file.size.asFileSize().formatHumanReadable(context)

        val mimeType = getMimeTypeFromPath(file.path)
        try {
            holder.icon.setImageResource(mimeType.icon.resourceId)
        } catch (e: Exception) {
            holder.icon.setImageResource(R.drawable.document_icon_white_24dp)
        }

        holder.card.setOnClickListener {
            onClick(file, mimeType)
        }
    }

    override fun getItemCount(): Int = files.size

    private fun getMimeTypeFromPath(path: String): MimeType {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        return mime?.asMimeTypeOrNull() ?: MimeType.GENERIC
    }
}
