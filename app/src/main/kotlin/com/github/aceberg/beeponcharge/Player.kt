package com.github.aceberg.beeponcharge

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.database.Cursor
import android.provider.OpenableColumns

class Player(private val context: Context) {

    private var mediaPlayer: MediaPlayer? = null

    private fun getSongUri(): Uri? {
        val songUri = context
            .getSharedPreferences("settings", Context.MODE_PRIVATE)
            .getString("song_uri", null)

        return songUri?.let { Uri.parse(it) }
    }

    fun getSongName(): String {
        var name = "No song selected"
        val uri = getSongUri() ?: return name

        val cursor: Cursor? = context.contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(
                    it.getColumnIndexOrThrow(OpenableColumns.DISPLAY_NAME)
                )
            }
        }

        return name
    }

    fun play(onComplete: (() -> Unit)? = null) {
        val uri = getSongUri() ?: return

        stop()

        mediaPlayer = MediaPlayer.create(context, uri)

        mediaPlayer?.setOnCompletionListener {
            stop()
            onComplete?.invoke()
        }

        mediaPlayer?.start()
    }

    fun stop() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    fun isPlaying(): Boolean {
        return mediaPlayer?.isPlaying == true
    }
}