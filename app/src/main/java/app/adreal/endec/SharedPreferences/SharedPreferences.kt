package app.adreal.endec.SharedPreferences

import android.content.Context
import androidx.preference.PreferenceManager

object SharedPreferences {

    private lateinit var prefs: android.content.SharedPreferences

    fun init(context: Context) {
        prefs = PreferenceManager.getDefaultSharedPreferences(context)
    }

    fun read(key: String, value: String): String? {
        return prefs.getString(key, value)
    }

    fun read(key: String, value: Int): Int {
        return prefs.getInt(key, value)
    }

    fun write(key: String, value: String) {
        val prefsEditor: android.content.SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putString(key, value)
            apply()
        }
    }

    fun write(key: String, value: Int) {
        val prefsEditor: android.content.SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            putInt(key, value)
            apply()
        }
    }

    fun deleteAll() {
        val prefsEditor: android.content.SharedPreferences.Editor = prefs.edit()
        with(prefsEditor) {
            clear()
            apply()
        }
    }
}