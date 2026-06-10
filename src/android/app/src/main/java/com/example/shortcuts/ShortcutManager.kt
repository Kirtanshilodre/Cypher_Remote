package com.example.shortcuts

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject

data class ShortcutItem(
    val id: String,
    val label: String,
    val ctrl: Boolean = false,
    val alt: Boolean = false,
    val shift: Boolean = false,
    val meta: Boolean = false, // Win or Cmd key
    val key: String // e.g., "t", "space", "enter", "f5"
) {
    /**
     * Converts a shortcut into a serializable JSON object
     */
    fun toJsonObject(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("label", label)
            put("ctrl", ctrl)
            put("alt", alt)
            put("shift", shift)
            put("meta", meta)
            put("key", key)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ShortcutItem {
            return ShortcutItem(
                id = obj.optString("id", UUID_generator()),
                label = obj.optString("label", "Shortcut"),
                ctrl = obj.optBoolean("ctrl", false),
                alt = obj.optBoolean("alt", false),
                shift = obj.optBoolean("shift", false),
                meta = obj.optBoolean("meta", false),
                key = obj.optString("key", "")
            )
        }
        
        private fun UUID_generator(): String = java.util.UUID.randomUUID().toString()
    }
}

data class ShortcutProfile(
    val id: String,
    val appName: String,
    val shortcuts: List<ShortcutItem>
) {
    fun toJsonObject(): JSONObject {
        val arr = JSONArray()
        shortcuts.forEach { arr.put(it.toJsonObject()) }
        return JSONObject().apply {
            put("id", id)
            put("appName", appName)
            put("shortcuts", arr)
        }
    }

    companion object {
        fun fromJsonObject(obj: JSONObject): ShortcutProfile {
            val list = mutableListOf<ShortcutItem>()
            val arr = obj.optJSONArray("shortcuts")
            if (arr != null) {
                for (i in 0 until arr.length()) {
                    val itemObj = arr.optJSONObject(i)
                    if (itemObj != null) {
                        list.add(ShortcutItem.fromJsonObject(itemObj))
                    }
                }
            }
            return ShortcutProfile(
                id = obj.optString("id", java.util.UUID.randomUUID().toString()),
                appName = obj.optString("appName", "Application"),
                shortcuts = list
            )
        }
    }
}

class ShortcutManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences("pc_remote_shortcuts", Context.MODE_PRIVATE)

    init {
        // Initialize default profiles if storage is completely empty
        if (!prefs.contains("shortcut_profiles")) {
            saveProfiles(getDefaultProfiles())
        }
    }

    fun loadProfiles(): List<ShortcutProfile> {
        val jsonStr = prefs.getString("shortcut_profiles", null) ?: return getDefaultProfiles()
        return try {
            val arr = JSONArray(jsonStr)
            val list = mutableListOf<ShortcutProfile>()
            for (i in 0 until arr.length()) {
                val obj = arr.optJSONObject(i)
                if (obj != null) {
                    list.add(ShortcutProfile.fromJsonObject(obj))
                }
            }
            list
        } catch (e: Exception) {
            Log.e("ShortcutManager", "Error parsing shortcuts json", e)
            getDefaultProfiles()
        }
    }

    fun saveProfiles(profiles: List<ShortcutProfile>) {
        try {
            val arr = JSONArray()
            profiles.forEach { arr.put(it.toJsonObject()) }
            prefs.edit().putString("shortcut_profiles", arr.toString()).apply()
        } catch (e: Exception) {
            Log.e("ShortcutManager", "Error saving profiles", e)
        }
    }

    fun resetToDefault() {
        saveProfiles(getDefaultProfiles())
    }

    private fun getDefaultProfiles(): List<ShortcutProfile> {
        return listOf(
            ShortcutProfile(
                id = "browser_profile",
                appName = "Web Browser (Chrome/Firefox)",
                shortcuts = listOf(
                    ShortcutItem("New Tab", "new_tab", ctrl = true, key = "t"),
                    ShortcutItem("Close Tab", "close_tab", ctrl = true, key = "w"),
                    ShortcutItem("Reopen Closed", "reopen_tab", ctrl = true, shift = true, key = "t"),
                    ShortcutItem("Next Tab", "next_tab", ctrl = true, key = "tab"),
                    ShortcutItem("Zoom In", "zoom_in", ctrl = true, key = "="),
                    ShortcutItem("Zoom Out", "zoom_out", ctrl = true, key = "-")
                )
            ),
            ShortcutProfile(
                id = "vlc_profile",
                appName = "VLC Media Player",
                shortcuts = listOf(
                    ShortcutItem("Play / Pause", "play", key = "space"),
                    ShortcutItem("Fullscreen", "fullscreen", key = "f"),
                    ShortcutItem("Volume Up", "vol_up", ctrl = true, key = "up"),
                    ShortcutItem("Volume Down", "vol_dn", ctrl = true, key = "down"),
                    ShortcutItem("Fast Forward 10s", "ff", alt = true, key = "right"),
                    ShortcutItem("Rewind 10s", "rw", alt = true, key = "left")
                )
            ),
            ShortcutProfile(
                id = "presentation_profile",
                appName = "Presentation (PowerPoint)",
                shortcuts = listOf(
                    ShortcutItem("Presenter View", "f5_presentation", key = "f5"),
                    ShortcutItem("Next Slide", "next_slide", key = "right"),
                    ShortcutItem("Prev Slide", "prev_slide", key = "left"),
                    ShortcutItem("Blank Screen", "blank_screen", key = "b"),
                    ShortcutItem("Exit Playback", "exit_presentation", key = "escape")
                )
            ),

            ShortcutProfile(
                id = "windows_profile",
                appName = "Windows Shortcuts",
                shortcuts = listOf(
                    ShortcutItem("Copy",        "win_copy",   ctrl = true,  key = "c"),
                    ShortcutItem("Paste",       "win_paste",  ctrl = true,  key = "v"),
                    ShortcutItem("Cut",         "win_cut",    ctrl = true,  key = "x"),
                    ShortcutItem("Undo",        "win_undo",   ctrl = true,  key = "z"),
                    ShortcutItem("Redo",        "win_redo",   ctrl = true,  shift = true, key = "z"),
                    ShortcutItem("Select All",  "win_all",    ctrl = true,  key = "a"),
                    ShortcutItem("Save",        "win_save",   ctrl = true,  key = "s"),
                    ShortcutItem("Alt+Tab",     "win_alttab", alt  = true,  key = "tab"),
                    ShortcutItem("Task Manager","win_taskman",ctrl = true,  shift = true, key = "escape"),
                    ShortcutItem("Lock Screen", "win_lock",   meta = true,  key = "l")
                )
            ),
            ShortcutProfile(
                id = "mac_profile",
                appName = "Mac Shortcuts (Cmd)",
                shortcuts = listOf(
                    ShortcutItem("Copy",         "mac_copy",    meta = true,  key = "c"),
                    ShortcutItem("Paste",        "mac_paste",   meta = true,  key = "v"),
                    ShortcutItem("Cut",          "mac_cut",     meta = true,  key = "x"),
                    ShortcutItem("Undo",         "mac_undo",    meta = true,  key = "z"),
                    ShortcutItem("Redo",         "mac_redo",    meta = true,  shift = true, key = "z"),
                    ShortcutItem("Select All",   "mac_all",     meta = true,  key = "a"),
                    ShortcutItem("Save",         "mac_save",    meta = true,  key = "s"),
                    ShortcutItem("Close Window", "mac_close",   meta = true,  key = "w"),
                    ShortcutItem("Quit App",     "mac_quit",    meta = true,  key = "q"),
                    ShortcutItem("Spotlight",    "mac_spot",    meta = true,  key = "space"),
                    ShortcutItem("Screenshot",   "mac_ss",      meta = true,  shift = true, key = "4"),
                    ShortcutItem("Mission Ctrl", "mac_mission", ctrl = true,  key = "up"),
                    ShortcutItem("Switch App",   "mac_switch",  meta = true,  key = "tab"),
                    ShortcutItem("New Tab",      "mac_newtab",  meta = true,  key = "t"),
                    ShortcutItem("Find",         "mac_find",    meta = true,  key = "f")
                )
            ),
        )
    }
}
