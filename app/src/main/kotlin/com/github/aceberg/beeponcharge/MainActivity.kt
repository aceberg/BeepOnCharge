package com.github.aceberg.beeponcharge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var batteryText: TextView

    private var mediaPlayer: MediaPlayer? = null
    private var alarmPlayed = false

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra("level", -1)
            val status = intent.getIntExtra("status", -1)

            batteryText.text = "$level%"

            val charging =
                status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
                status == android.os.BatteryManager.BATTERY_STATUS_FULL

            if (!charging) {
                alarmPlayed = false
                return
            }

            val settings = getSharedPreferences("settings", MODE_PRIVATE)
            val alarmLevel = settings.getInt("alarm_level", 80)

            if (level >= alarmLevel && !alarmPlayed) {
                playAlarm()
                alarmPlayed = true
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        batteryText = findViewById(R.id.battery)

        findViewById<Button>(R.id.stop).setOnClickListener {
            stopAlarm()
        }

        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    private fun playAlarm() {
        val songUri = getSharedPreferences("settings", MODE_PRIVATE)
            .getString("song_uri", null) ?: return

        stopAlarm()

        mediaPlayer = MediaPlayer.create(
            this,
            Uri.parse(songUri)
        )

        mediaPlayer?.setOnCompletionListener {
            stopAlarm()
        }

        mediaPlayer?.start()
    }

    private fun stopAlarm() {
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun onDestroy() {
        stopAlarm()
        unregisterReceiver(batteryReceiver)
        super.onDestroy()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_settings) {
            startActivity(Intent(this, SettingsView::class.java))
            return true
        }

        return super.onOptionsItemSelected(item)
    }
}