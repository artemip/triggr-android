package co.fether.triggr

import android.preference.{CheckBoxPreference, SwitchPreference, Preference, PreferenceActivity}
import android.os.{Build, Bundle}
import android.preference.Preference.OnPreferenceChangeListener
import android.content.{Context, Intent, DialogInterface}
import android.app.AlertDialog.Builder
import co.fether.triggr.json.{InAppProductInfo, InAppPurchaseInfo}
import android.app.Activity
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

    var advancedNotificationsEnabled = true

    val allProducts =
      TriggrService.getProductInfo(
        List(TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS,
          TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS))

    val purchasedProducts = TriggrService.getPurchasedProducts

    purchasedProducts.find(pid => pid == TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS) match {
      case Some(_) => {
        enableWhatsappPreference()
        advancedNotificationsEnabled = true
      }
      case None => {
        // User has not purchased WhatsApp notifications
        allProducts.find(p => p.productId == TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS) match {
          case Some(whatsappProduct) => {
            enableWhatsappPurchasePreference(whatsappProduct)
          }
          case None => {
            disableWhatsappPurchasePreference()
          }
        }
      }
    }

    purchasedProducts.find(pid => pid == TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS) match {
      case Some(_) =>
        enableSnapchatPreference()
        advancedNotificationsEnabled = true
      case None => {
        // User has not purchased SnapChat notifications
        allProducts.find(p => p.productId == TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS) match {
          case Some(snapchatProduct) => {
            enableSnapchatPurchasePreference(snapchatProduct)
          }
          case None => {
            disableSnapchatPurchasePreference()
          }
        }
      }
    }

    Log.d(tag, "Advanced notifications are " + (if (advancedNotificationsEnabled) "enabled" else "disabled"))

    if(advancedNotificationsEnabled)
      verifyAccessibilityServiceEnabled()
  }

  private def enableWhatsappPurchasePreference(whatsappProduct : InAppProductInfo) {
    Log.d(tag, "WhatsApp notifications not purchased. Enabling purchase preference")

    whatsappPreference.setSummary(getString(R.string.purchase_notification_desc) + " " + whatsappProduct.price + "!")
    whatsappPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.whatsapp_notifications_pref_title)
          .setMessage(getString(R.string.purchase_notification_confirm_whatsapp) + " " + whatsappProduct.price + "?")
          .setPositiveButton(Util.getString(R.string.buy_text), new DialogInterface.OnClickListener() {
          override def onClick(dialog : DialogInterface, which : Int) {
            val purchaseIntent = TriggrService.getPurchaseIntent(TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS)
            if (purchaseIntent.isDefined) {
              startIntentSenderForResult(purchaseIntent.get.getIntentSender,
                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0))
            }
          }
        })
        .setNegativeButton(Util.getString(R.string.cancel_text), null)
        .show()

        false
      }
    })
  }

  private def enableWhatsappPreference() {
    Log.d(tag, "WhatsApp notifications purchased. Enabling preference")

    whatsappPreference.setEnabled(true)
    whatsappPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    whatsappPreference.setOnPreferenceChangeListener(null)
  }

  private def disableWhatsappPurchasePreference() {
    Log.d(tag, "Disabling WhatsApp purchase preference")

    whatsappPreference.setEnabled(false)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      whatsappPreference.asInstanceOf[SwitchPreference].setChecked(false)
    else
      whatsappPreference.asInstanceOf[CheckBoxPreference].setChecked(false)
  }

  private def enableSnapchatPurchasePreference(snapchatProduct : InAppProductInfo) {
    Log.d(tag, "SnapChat notifications not purchased. Enabling purchase preference")

    snapchatPreference.setSummary(getString(R.string.purchase_notification_desc) + " " + snapchatProduct.price + "!")
    snapchatPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.snapchat_notifications_pref_title)
          .setMessage(getString(R.string.purchase_notification_confirm_snapchat) + " " + snapchatProduct.price + "?")
          .setPositiveButton(Util.getString(R.string.buy_text), new DialogInterface.OnClickListener() {
          override def onClick(dialog : DialogInterface, which : Int) {
            val purchaseIntent = TriggrService.getPurchaseIntent(TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS)
            if (purchaseIntent.isDefined) {
              startIntentSenderForResult(purchaseIntent.get.getIntentSender,
                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0))
            }
          }
        })
        .setNegativeButton(Util.getString(R.string.cancel_text), null)
        .show()

        false
      }
    })
  }

  private def enableSnapchatPreference() {
    Log.d(tag, "SnapChat notifications purchased. Enabling preference")
    snapchatPreference.setEnabled(true)
    snapchatPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    snapchatPreference.setOnPreferenceChangeListener(null)
  }

  private def disableSnapchatPurchasePreference() {
    Log.d(tag, "Disabling SnapChat purchase preference")
    snapchatPreference.setEnabled(false)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      snapchatPreference.asInstanceOf[SwitchPreference].setChecked(false)
    else
      snapchatPreference.asInstanceOf[CheckBoxPreference].setChecked(false)
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

  override def onActivityResult(requestCode : Int, resultCode : Int, data : Intent) {
    Log.d(tag, "Received activity result code: " + resultCode)

    if(resultCode == Activity.RESULT_OK && requestCode == 1001) {
      val inappPurchaseData = data.getStringExtra("INAPP_PURCHASE_DATA")
      Log.d(tag, "Received activity result data: " + inappPurchaseData)

      // Purchase made
      val purchaseInfo = new InAppPurchaseInfo().deserialize(inappPurchaseData)

      Log.d("co.fether.triggr.SettingsActivity", "Received Purchase Info: " + inappPurchaseData)
      Log.d("co.fether.triggr.SettingsActivity", "De-serialized Purchase Info: " + purchaseInfo.serialize())

      var advancedNotificationsEnabled = false
      purchaseInfo.productId match {
        case TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS => {
          advancedNotificationsEnabled = true

          enableWhatsappPreference()

          if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
            whatsappPreference.asInstanceOf[SwitchPreference].setChecked(true)
          else
            whatsappPreference.asInstanceOf[CheckBoxPreference].setChecked(true)

          EventActor ! EventActor.WhatsAppMessage(getString(R.string.purchase_whatsapp_notifications_desktop_message), "")
        }
        case TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS => {
          advancedNotificationsEnabled = true

          enableSnapchatPreference()

          if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
            snapchatPreference.asInstanceOf[SwitchPreference].setChecked(true)
          else
            snapchatPreference.asInstanceOf[CheckBoxPreference].setChecked(true)

          EventActor ! EventActor.SnapchatMessage(getString(R.string.purchase_snapchat_notifications_desktop_message), "")
        }
      }

      if(advancedNotificationsEnabled)
        verifyAccessibilityServiceEnabled()
    }
  }
}
