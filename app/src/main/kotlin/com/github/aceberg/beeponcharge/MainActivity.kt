package com.github.aceberg.beeponcharge

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var batteryText: TextView
    private lateinit var songName: TextView
    private lateinit var player: Player

    private var alarmPlayed = false
    private var alarmLevel = 80

    override fun onResume() {
        super.onResume()
        updateSettings()

        registerReceiver(
            batteryReceiver,
            IntentFilter(Intent.ACTION_BATTERY_CHANGED)
        )
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(batteryReceiver)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        player = Player(this)

        batteryText = findViewById(R.id.battery)
        songName = findViewById(R.id.song_name)

        findViewById<Button>(R.id.stop).setOnClickListener {
            player.stop()
        }

        findViewById<Button>(R.id.exit).setOnClickListener {
            finish()
        }
    }

    private val batteryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val level = intent.getIntExtra("level", -1)
            val status = intent.getIntExtra("status", -1)

            if (level >= 0) {
                updateBattery(level, status)
            }
        }
    }

    private fun updateBattery(level: Int, status: Int) {
        batteryText.text = "$level%"

        val charging =
            status == android.os.BatteryManager.BATTERY_STATUS_CHARGING ||
            status == android.os.BatteryManager.BATTERY_STATUS_FULL

        batteryText.setTextColor(
            when {
                level < 30 -> ContextCompat.getColor(this@MainActivity, R.color.battery_red)
                !charging -> ContextCompat.getColor(this@MainActivity, R.color.battery_grey)
                level < alarmLevel -> ContextCompat.getColor(this@MainActivity, R.color.battery_green)
                else -> ContextCompat.getColor(this@MainActivity, R.color.battery_orange)
            }
        )

        if (level >= alarmLevel && !alarmPlayed && charging) {
            player.play()
            alarmPlayed = true
        }
    }

    private fun updateSettings() {
        val settings = getSharedPreferences("settings", MODE_PRIVATE)
        alarmLevel = settings.getInt("alarm_level", 80)
        alarmPlayed = false

        songName.text = player.getSongName()
    }

    override fun onDestroy() {
        player.stop()
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