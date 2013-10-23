package co.fether.triggr

import android.preference.PreferenceActivity
import android.os.Bundle
import android.graphics.Typeface

class SettingsActivity extends PreferenceActivity {
  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.preferences)
  }
}
