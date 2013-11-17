package co.fether.triggrtrial

import android.preference.{CheckBoxPreference, SwitchPreference, Preference, PreferenceActivity}
import android.os.{Build, Bundle}
import android.preference.Preference.OnPreferenceChangeListener
import android.content.{Context, Intent, DialogInterface}
import android.app.AlertDialog.Builder

import android.util.Log
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.TextUtils

class SettingsActivity extends PreferenceActivity {
  val tag = getClass.getCanonicalName

  var whatsappPreference : Preference = null
  var snapchatPreference : Preference = null

  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)

    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1) {
      addPreferencesFromResource(R.xml.preferences)
      whatsappPreference = findPreference(Preferences.PREF_WHATSAPP_NOTIFICATIONS).asInstanceOf[SwitchPreference]
      snapchatPreference = findPreference(Preferences.PREF_SNAPCHAT_NOTIFICATIONS).asInstanceOf[SwitchPreference]
    } else {
      addPreferencesFromResource(R.xml.gingerbread_preferences)
      whatsappPreference = findPreference(Preferences.PREF_WHATSAPP_NOTIFICATIONS).asInstanceOf[CheckBoxPreference]
      snapchatPreference = findPreference(Preferences.PREF_SNAPCHAT_NOTIFICATIONS).asInstanceOf[CheckBoxPreference]
    }

    enableWhatsappPreference()
    enableSnapchatPreference()
  }

  private def enableWhatsappPreference() {
    whatsappPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    whatsappPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, newValue: scala.Any): Boolean = {
        if (newValue.asInstanceOf[Boolean]) {
          verifyAccessibilityServiceEnabled()
        }

        true
      }
    })
  }

  private def enableSnapchatPreference() {
    snapchatPreference.setSummary(R.string.whatsapp_notifications_pref_desc)

    snapchatPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, newValue: scala.Any): Boolean = {
        if (newValue.asInstanceOf[Boolean]) {
          verifyAccessibilityServiceEnabled()
        }

        true
      }
    })
  }

  private def verifyAccessibilityServiceEnabled() {
    if(!isAccessibilityServiceEnabled) {
      new Builder(SettingsActivity.this)
        .setIcon(android.R.drawable.ic_dialog_info)
        .setTitle(R.string.enable_accessibility_service_title)
        .setMessage(R.string.enable_accessibility_service_message)
        .setPositiveButton(Util.getString(R.string.ok_text), new DialogInterface.OnClickListener() {
        override def onClick(dialog : DialogInterface, which : Int) {
          val accIntent = new Intent(android.provider.Settings.ACTION_ACCESSIBILITY_SETTINGS)
          startActivityForResult(accIntent, 0)
        }
      })
      .setNegativeButton(Util.getString(R.string.cancel_text), null)
      .show()
    }
  }

  private def isAccessibilityServiceEnabled : Boolean = {
    val serviceName = classOf[TriggrNotificationListener].getPackage.getName + "/" + classOf[TriggrNotificationListener].getCanonicalName
    val context = this

    val accessibilityEnabled = {
      try {
        Settings.Secure.getInt(context.getApplicationContext.getContentResolver, Settings.Secure.ACCESSIBILITY_ENABLED)
      } catch {
        case e: SettingNotFoundException => {
          Log.d(tag, "Could not find accessibility setting: " + e.getMessage)
          0
        }
        case _: Throwable => {
          0
        }
      }
    }

    if (accessibilityEnabled == 1) {
      Log.d(tag, "Accessibility is enabled.")
      val settingValue = Settings.Secure.getString(context.getApplicationContext.getContentResolver, Settings.Secure.ENABLED_ACCESSIBILITY_SERVICES)

      if(settingValue != null) {
        val splitter = new TextUtils.SimpleStringSplitter(':')
        splitter.setString(settingValue)

        while(splitter.hasNext) {
          val accService = splitter.next()

          Log.d(tag, "Found accessibility service: " + accService)
          if (accService.equalsIgnoreCase(serviceName)) {
            Log.d(tag, "Triggr accessibility service is enabled!")
            return true
          }
        }
      }
    }

    false
  }
}
