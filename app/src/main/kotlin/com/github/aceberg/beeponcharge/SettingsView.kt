package com.github.aceberg.beeponcharge

import android.content.Intent
import android.database.Cursor
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.SeekBar
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

class SettingsView : AppCompatActivity() {

    private lateinit var songName: TextView
    private lateinit var playSong: Button
    private lateinit var alarmLevel: SeekBar
    private lateinit var alarmLevelText: TextView

    private var mediaPlayer: MediaPlayer? = null

    private val pickSong = registerForActivityResult(
        ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            contentResolver.takePersistableUriPermission(
                it,
                Intent.FLAG_GRANT_READ_URI_PERMISSION
            )

            getSharedPreferences("settings", MODE_PRIVATE)
                .edit()
                .putString("song_uri", it.toString())
                .apply()

            songName.text = getSongName(it)
            playSong.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        songName = findViewById(R.id.song_name)
        playSong = findViewById(R.id.play_song)

        val settings = getSharedPreferences("settings", MODE_PRIVATE)

        val savedSong = settings.getString("song_uri", null)

        if (savedSong != null) {
            songName.text = getSongName(Uri.parse(savedSong))
            playSong.isEnabled = true
        } else {
            songName.text = "No song selected"
            playSong.isEnabled = false
        }

        findViewById<Button>(R.id.pick_song).setOnClickListener {
            pickSong.launch(arrayOf("audio/*"))
        }

        playSong.setOnClickListener {
            if (mediaPlayer?.isPlaying == true) {
                stopSong()
            } else {
                playSelectedSong()
            }
        }

        alarmLevel = findViewById(R.id.alarm_level)
        alarmLevelText = findViewById(R.id.alarm_level_text)

        val savedLevel = settings.getInt("alarm_level", 80)

        alarmLevel.progress = savedLevel
        alarmLevelText.text = "$savedLevel%"

        alarmLevel.setOnSeekBarChangeListener(
            object : SeekBar.OnSeekBarChangeListener {

                override fun onProgressChanged(
                    seekBar: SeekBar?,
                    progress: Int,
                    fromUser: Boolean
                ) {
                    alarmLevelText.text = "$progress%"
                }

                override fun onStartTrackingTouch(seekBar: SeekBar?) {
                }

                override fun onStopTrackingTouch(seekBar: SeekBar?) {
                    settings.edit()
                        .putInt("alarm_level", alarmLevel.progress)
                        .apply()
                }
            }
        )
    }

    private fun playSelectedSong() {
        val savedSong = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("song_uri", null) ?: return

        stopSong()

        try {
            mediaPlayer = MediaPlayer()

            mediaPlayer?.setDataSource(
                this,
                Uri.parse(savedSong)
            )

            mediaPlayer?.setOnPreparedListener {
                it.start()
                playSong.text = "Stop"
            }

            mediaPlayer?.setOnCompletionListener {
                stopSong()
            }

            mediaPlayer?.setOnErrorListener { _, what, extra ->
                playSong.text = "Play"
                true
            }

            mediaPlayer?.prepareAsync()

        } catch (e: Exception) {
            mediaPlayer?.release()
            mediaPlayer = null
            playSong.text = "Play"
        }
    }

    private fun stopSong() {
        mediaPlayer?.release()
        mediaPlayer = null
        playSong.text = "Play"
    }

    private fun getSongName(uri: Uri): String {
        var name = "Unknown song"

        val cursor: Cursor? = contentResolver.query(
            uri,
            arrayOf(OpenableColumns.DISPLAY_NAME),
            null,
            null,
            null
        )

        cursor?.use {
            if (it.moveToFirst()) {
                name = it.getString(0)
            }
        }

        return name
    }

    override fun onDestroy() {
        stopSong()
        super.onDestroy()
    }
}