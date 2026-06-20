package me.mumin.android.files.filelist

import android.content.Context
import android.provider.MediaStore
import me.mumin.android.files.file.FileItem
import me.mumin.android.files.file.loadFileItem
import me.mumin.android.files.util.CloseableLiveData
import me.mumin.android.files.util.Failure
import me.mumin.android.files.util.Loading
import me.mumin.android.files.util.Stateful
import me.mumin.android.files.util.Success
import me.mumin.android.files.util.valueCompat
import java.io.File
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.Future
import me.mumin.android.files.app.application

class MediaStoreCategoryLiveData(
    private val category: String
) : CloseableLiveData<Stateful<List<FileItem>>>() {
    private var future: Future<Unit>? = null
    private var limit = 100
    private var isFullyLoaded = false

    init {
        loadValue()
    }

    fun loadMore() {
        if (!isFullyLoaded) {
            limit += 100
            loadValue()
        }
    }

    fun loadValue() {
        future?.cancel(true)
        val currentList = value?.value ?: emptyList()
        value = Loading(currentList)
        future = executor.submit<Unit> {
            val fileList = mutableListOf<FileItem>()
            try {
                val uri = MediaStore.Files.getContentUri("external")
                val projection = arrayOf(MediaStore.Files.FileColumns.DATA)
                
                val selection: String
                val selectionArgs: Array<String>?
                
                when (category) {
                    "Images" -> {
                        selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
                        selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE.toString())
                    }
                    "Videos" -> {
                        selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
                        selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO.toString())
                    }
                    "Audio" -> {
                        selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
                        selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_AUDIO.toString())
                    }
                    "Documents" -> {
                        selection = "${MediaStore.Files.FileColumns.MEDIA_TYPE} = ?"
                        selectionArgs = arrayOf(MediaStore.Files.FileColumns.MEDIA_TYPE_DOCUMENT.toString())
                    }
                    "Downloads" -> {
                        // Downloads might not have a reliable MEDIA_TYPE, so we check path
                        selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
                        selectionArgs = arrayOf("%/Download/%")
                    }
                    "APKs" -> {
                        selection = "${MediaStore.Files.FileColumns.DATA} LIKE ?"
                        selectionArgs = arrayOf("%.apk")
                    }
                    else -> {
                        selection = ""
                        selectionArgs = null
                    }
                }

                application.contentResolver.query(
                    uri,
                    projection,
                    selection,
                    selectionArgs,
                    "${MediaStore.Files.FileColumns.DATE_MODIFIED} DESC"
                )?.use { cursor ->
                    val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Files.FileColumns.DATA)
                    var count = 0
                    while (count < limit && cursor.moveToNext()) {
                        val path = cursor.getString(dataColumn)
                        try {
                            val java8Path = java8.nio.file.Paths.get(path)
                            fileList.add(java8Path.loadFileItem())
                            count++
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                    isFullyLoaded = !cursor.moveToNext()
                }
                
                postValue(Success(fileList))
            } catch (e: Exception) {
                postValue(Failure(valueCompat.value, e))
            }
        }
    }

    override fun close() {
        future?.cancel(true)
    }

    companion object {
        private val executor: ExecutorService = Executors.newSingleThreadExecutor()
    }
}
