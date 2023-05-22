package app.adreal.endec.Fragments

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import app.adreal.endec.R

class SettingsFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}