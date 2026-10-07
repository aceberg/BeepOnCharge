package com.github.aceberg.beeponcharge

import android.content.Intent
import android.net.Uri
import android.os.Bundle
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

    private lateinit var player: Player

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

            songName.text = player.getSongName()
            playSong.isEnabled = true
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_view)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.title = "Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        toolbar.setNavigationOnClickListener {
            finish()
        }

        player = Player(this)

        songName = findViewById(R.id.song_name)
        playSong = findViewById(R.id.play_song)

        val settings = getSharedPreferences("settings", MODE_PRIVATE)


        songName.text = player.getSongName()


        findViewById<Button>(R.id.pick_song).setOnClickListener {
            pickSong.launch(arrayOf("audio/*"))
        }

        playSong.setOnClickListener {
            if (player.isPlaying()) {
                player.stop()
                playSong.text = "Play"
            } else {
                player.play() {
                    playSong.text = "Play"
                }

                playSong.text = "Stop"
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

    override fun onDestroy() {
        player.stop()
        super.onDestroy()
    }
}