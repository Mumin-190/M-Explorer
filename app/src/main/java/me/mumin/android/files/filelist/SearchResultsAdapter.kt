package me.mumin.android.files.filelist

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.MimeTypeMap
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import me.mumin.android.files.R
import me.mumin.android.files.file.MimeType
import me.mumin.android.files.file.asMimeTypeOrNull
import me.mumin.android.files.file.icon
import me.mumin.android.files.file.asFileSize
import java.text.DateFormat
import java.util.Date

class SearchResultsAdapter(
    private val context: Context,
    private var files: List<RecentFile>,
    private val onClick: (RecentFile, MimeType) -> Unit
) : RecyclerView.Adapter<SearchResultsAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val layout: View = view.findViewById(R.id.search_item_layout)
        val icon: ImageView = view.findViewById(R.id.search_item_icon)
        val name: TextView = view.findViewById(R.id.search_item_name)
        val details: TextView = view.findViewById(R.id.search_item_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.search_result_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val file = files[position]
        holder.name.text = file.name

        val sizeStr = file.size.asFileSize().formatHumanReadable(context)
        val dateStr = DateFormat.getDateInstance().format(Date(file.dateModified * 1000))
        holder.details.text = "$sizeStr • $dateStr"

        val mimeType = getMimeTypeFromPath(file.path)
        try {
            holder.icon.setImageResource(mimeType.icon.resourceId)
        } catch (e: Exception) {
            holder.icon.setImageResource(R.drawable.document_icon_white_24dp)
        }

        holder.layout.setOnClickListener {
            onClick(file, mimeType)
        }
    }

    override fun getItemCount(): Int = files.size

    fun updateData(newFiles: List<RecentFile>) {
        files = newFiles
        notifyDataSetChanged()
    }

    private fun getMimeTypeFromPath(path: String): MimeType {
        val extension = MimeTypeMap.getFileExtensionFromUrl(path)
        val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension.lowercase())
        return mime?.asMimeTypeOrNull() ?: MimeType.GENERIC
    }
}
