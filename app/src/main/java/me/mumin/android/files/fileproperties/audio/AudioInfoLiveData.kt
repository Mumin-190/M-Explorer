/*
 * Copyright (c) 2020 Mumin <mumin@example.com>
 * All Rights Reserved.
 */

package me.mumin.android.files.fileproperties.audio

import android.media.MediaMetadataRetriever
import android.os.AsyncTask
import android.os.Build
import java.time.Duration
import java8.nio.file.Path
import me.mumin.android.files.compat.METADATA_KEY_SAMPLERATE
import me.mumin.android.files.compat.use
import me.mumin.android.files.fileproperties.PathObserverLiveData
import me.mumin.android.files.fileproperties.extractMetadataNotBlank
import me.mumin.android.files.util.Failure
import me.mumin.android.files.util.Loading
import me.mumin.android.files.util.Stateful
import me.mumin.android.files.util.Success
import me.mumin.android.files.util.setDataSource
import me.mumin.android.files.util.valueCompat

class AudioInfoLiveData(path: Path) : PathObserverLiveData<Stateful<AudioInfo>>(path) {
    init {
        loadValue()
        observe()
    }

    override fun loadValue() {
        value = Loading(value?.value)
        AsyncTask.THREAD_POOL_EXECUTOR.execute {
            val value = try {
                val audioInfo = MediaMetadataRetriever().use { retriever ->
                    retriever.setDataSource(path)
                    val title = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_TITLE
                    )
                    val artist = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ARTIST
                    )
                    val album = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ALBUM
                    )
                    val albumArtist = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_ALBUMARTIST
                    )
                    val composer = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_COMPOSER
                    )
                    val discNumber = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_DISC_NUMBER
                    )
                    val trackNumber = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_CD_TRACK_NUMBER
                    )
                    val year = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_YEAR
                    )
                    val genre = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_GENRE
                    )
                    val duration = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_DURATION
                    )?.toLongOrNull()?.let { Duration.ofMillis(it) }
                    val bitRate = retriever.extractMetadataNotBlank(
                        MediaMetadataRetriever.METADATA_KEY_BITRATE
                    )?.toIntOrNull()
                    val sampleRate = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        retriever.extractMetadataNotBlank(
                            MediaMetadataRetriever::class.METADATA_KEY_SAMPLERATE
                        )?.toIntOrNull()
                    } else {
                        null
                    }
                    AudioInfo(
                        title, artist, album, albumArtist, composer, discNumber, trackNumber, year,
                        genre, duration, bitRate, sampleRate
                    )
                }
                Success(audioInfo)
            } catch (e: Exception) {
                Failure(valueCompat.value, e)
            }
            postValue(value)
        }
    }
}
