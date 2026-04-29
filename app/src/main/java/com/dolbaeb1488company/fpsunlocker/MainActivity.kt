package com.dolbaeb1488company.fpsunlocker

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.edit
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.switchmaterial.SwitchMaterial

class MainActivity : AppCompatActivity() {

    private lateinit var unlockerScreen: ConstraintLayout
    private lateinit var customizationScreen: ConstraintLayout
    private lateinit var adapter: CustomSquaresAdapter
    private lateinit var musicAdapter: MusicAppsAdapter
    private val squareList = mutableListOf<String>()
    private val musicAppList = mutableListOf<String>()

    private val SETTING_FP_ICON = "light_cover_customize_fp_icon"
    private val SETTING_MUSIC_WIDGET = "musicwidget_list_pkg_type_key"
    private val SPLIT = "#split#"

    private val uiUpdateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val isChecked = intent.getBooleanExtra("isChecked", false)
            updateFpsSwitch(isChecked)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        unlockerScreen = findViewById(R.id.unlocker_screen)
        customizationScreen = findViewById(R.id.customization_screen)

        setupUnlocker()
        setupCustomization()
        setupNavigation()
        checkFirstRun()

        findViewById<ImageButton>(R.id.github_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://github.com/ewfawfasdf/VivoIQOO144FPSUnlocker/".toUri()))
        }

        findViewById<ImageButton>(R.id.telegram_button).setOnClickListener {
            startActivity(Intent(Intent.ACTION_VIEW,
                "https://t.me/Idontcareaboutmyname".toUri()))
        }

        val filter = IntentFilter("com.dolbaeb1488company.fpsunlocker.UPDATE_UI")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(uiUpdateReceiver, filter, RECEIVER_NOT_EXPORTED)
        } else {
            @Suppress("UnspecifiedRegisterReceiverFlag")
            registerReceiver(uiUpdateReceiver, filter)
        }
    }

    override fun onDestroy() {
        unregisterReceiver(uiUpdateReceiver)
        super.onDestroy()
    }

    private fun checkFirstRun() {
        val prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE)
        if (prefs.getBoolean("isFirstRun", true)) {
            AlertDialog.Builder(this)
                .setTitle(R.string.setup_guide_title)
                .setMessage(R.string.setup_guide_msg)
                .setPositiveButton(R.string.ok) { _, _ ->
                    prefs.edit { putBoolean("isFirstRun", false) }
                }
                .setCancelable(false)
                .show()
        }
    }

    private fun setupNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_nav)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_unlocker -> {
                    unlockerScreen.visibility = View.VISIBLE
                    customizationScreen.visibility = View.GONE
                    findViewById<TextView>(R.id.subtitle_text).text = getString(R.string.unlocker)
                    true
                }
                R.id.nav_customization -> {
                    unlockerScreen.visibility = View.GONE
                    customizationScreen.visibility = View.VISIBLE
                    findViewById<TextView>(R.id.subtitle_text).text = getString(R.string.tweaks)
                    loadSquares()
                    loadMusicApps()
                    adapter.notifyDataSetChanged()
                    musicAdapter.updateData(musicAppList)
                    true
                }
                else -> false
            }
        }
        // Set initial subtitle
        findViewById<TextView>(R.id.subtitle_text).text = getString(R.string.unlocker)
    }

    private fun setupUnlocker() {
        val fpsSwitch = findViewById<SwitchMaterial>(R.id.fps_switch)
        val currentValue = Settings.System.getString(contentResolver, "gamecube_frame_interpolation_for_sr")
        val isChecked = currentValue == "1:1::72:144"
        
        updateFpsSwitch(isChecked)

        fpsSwitch.setOnCheckedChangeListener { _, checked ->
            setSystemSetting(checked)
        }
        
        // Ensure service is running
        startService(Intent(this, FpsService::class.java))
    }

    private fun updateFpsSwitch(isChecked: Boolean) {
        val fpsSwitch = findViewById<SwitchMaterial>(R.id.fps_switch)
        val valueText = findViewById<TextView>(R.id.value_text)
        val onLabel = findViewById<TextView>(R.id.on_label)
        val offLabel = findViewById<TextView>(R.id.off_label)

        fpsSwitch.isChecked = isChecked
        valueText.text = getString(R.string.fps_label, if (isChecked) "144" else "0-120")
        onLabel.setTextColor(if (isChecked) "#34D399".toColorInt() else "#6B7280".toColorInt())
        offLabel.setTextColor(if (isChecked) "#6B7280".toColorInt() else "#34D399".toColorInt())
    }

    private fun setSystemSetting(isChecked: Boolean) {
        try {
            Settings.System.putString(contentResolver, "gamecube_frame_interpolation_for_sr", if (isChecked) "1:1::72:144" else "0:-1:0:0:0")
            updateFpsSwitch(isChecked)
            
            // Notification update will happen in service if it reacts to setting change or if we trigger it
            // Re-triggering buildNotification by notifying the service
            startService(Intent(this, FpsService::class.java))
            
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "err: ${e.message}", e)
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
            updateFpsSwitch(!isChecked)
        }
    }

    private fun setupCustomization() {
        val recyclerView = findViewById<RecyclerView>(R.id.squares_recycler)
        val musicRecyclerView = findViewById<RecyclerView>(R.id.music_apps_recycler)
        findViewById<FloatingActionButton>(R.id.add_square_fab).visibility = View.GONE

        loadSquares()
        loadMusicApps()

        adapter = CustomSquaresAdapter(squareList,
            onItemClick = { index, value -> showEditDialog(index, value) },
            onItemLongClick = { index -> showDeleteDialog(index) },
            onAddClick = { showEditDialog(-1, "") }
        )
        recyclerView.adapter = adapter

        musicAdapter = MusicAppsAdapter(packageManager, 
            onRemoveClick = { index ->
                musicAppList.removeAt(index)
                musicAdapter.updateData(musicAppList)
                saveMusicApps()
            },
            onAddClick = { showAppPickerDialog() }
        )
        musicRecyclerView.adapter = musicAdapter
        musicAdapter.updateData(musicAppList)
    }

    private fun loadSquares() {
        val rawValue = Settings.System.getString(contentResolver, SETTING_FP_ICON) ?: ""
        squareList.clear()
        if (rawValue.isNotEmpty()) {
            val parts = rawValue.split(SPLIT).filter { it.isNotEmpty() }
            squareList.addAll(parts)
        }
    }

    private fun loadMusicApps() {
        val rawValue = Settings.System.getString(contentResolver, SETTING_MUSIC_WIDGET) ?: ""
        musicAppList.clear()
        if (rawValue.isNotEmpty()) {
            try {
                val cleaned = rawValue.removePrefix("[").removeSuffix("]")
                if (cleaned.isNotEmpty()) {
                    val parts = cleaned.split(",").map { it.trim().removeSurrounding("\"") }
                    musicAppList.addAll(parts.filter { it.isNotEmpty() })
                }
            } catch (e: Exception) {
                Log.e("FPSUnlocker", "Music parse err: ${e.message}")
            }
        }
    }

    private fun saveSquares() {
        val newValue = if (squareList.isEmpty()) "" else squareList.joinToString(SPLIT) + SPLIT
        try {
            Settings.System.putString(contentResolver, SETTING_FP_ICON, newValue)
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Save err: ${e.message}")
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun saveMusicApps() {
        val newValue = musicAppList.joinToString(separator = "\",\"", prefix = "[\"", postfix = "\"]")
        val finalValue = if (musicAppList.isEmpty()) "[]" else newValue
        try {
            Settings.System.putString(contentResolver, SETTING_MUSIC_WIDGET, finalValue)
        } catch (e: Exception) {
            Log.e("FPSUnlocker", "Music Save err: ${e.message}")
            Toast.makeText(this, getString(R.string.permission_denied), Toast.LENGTH_LONG).show()
        }
    }

    private fun showAppPickerDialog() {
        val pm = packageManager
        val installedApps = pm.getInstalledApplications(PackageManager.GET_META_DATA)
        val appItems = installedApps
            .filter { (it.flags and ApplicationInfo.FLAG_SYSTEM) == 0 || it.packageName.contains("music") }
            .map { AppItem(it, it.loadLabel(pm).toString(), musicAppList.contains(it.packageName)) }
            .sortedBy { it.name }

        val view = LayoutInflater.from(this).inflate(R.layout.dialog_app_picker, null)
        val rv = view.findViewById<RecyclerView>(R.id.picker_recycler)
        val pickerAdapter = AppPickerAdapter(pm, appItems)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = pickerAdapter

        AlertDialog.Builder(this)
            .setTitle(R.string.select_music_apps)
            .setView(view)
            .setPositiveButton(R.string.add_selected) { _, _ ->
                val selected = pickerAdapter.getSelectedPackages()
                musicAppList.clear()
                musicAppList.addAll(selected)
                musicAdapter.updateData(musicAppList)
                saveMusicApps()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showEditDialog(index: Int, currentValue: String) {
        val view = LayoutInflater.from(this).inflate(R.layout.dialog_edit_value, null)
        val editText = view.findViewById<EditText>(R.id.edit_value)
        editText.setText(currentValue)

        AlertDialog.Builder(this)
            .setTitle(if (index == -1) R.string.add_icon else R.string.edit_icon)
            .setView(view)
            .setPositiveButton(R.string.save) { _, _ ->
                val newValue = editText.text.toString()
                if (newValue.isNotEmpty()) {
                    if (index == -1) {
                        squareList.add(newValue)
                    } else {
                        squareList[index] = newValue
                    }
                    adapter.notifyDataSetChanged()
                    saveSquares()
                }
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun showDeleteDialog(index: Int) {
        AlertDialog.Builder(this)
            .setTitle(R.string.delete_icon_title)
            .setMessage(R.string.delete_icon_msg)
            .setPositiveButton(R.string.delete) { _, _ ->
                squareList.removeAt(index)
                adapter.notifyDataSetChanged()
                saveSquares()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }
}
