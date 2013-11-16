package co.fether.triggr

import android.preference.{CheckBoxPreference, SwitchPreference, Preference, PreferenceActivity}
import android.os.{Build, Bundle}
import android.preference.Preference.OnPreferenceChangeListener
import android.content.{Intent, DialogInterface}
import android.app.AlertDialog.Builder
import co.fether.triggr.json.{InAppProductInfo, InAppPurchaseInfo}
import android.app.Activity
import android.util.Log

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

    val allProducts =
      TriggrService.getProductInfo(
        List(TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS,
          TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS))
    val purchasedProducts = TriggrService.getPurchasedProducts

    purchasedProducts.find(pid => pid == TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS) match {
      case Some(_) => {
        enableWhatsappPreference()
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
  }

  private def enableWhatsappPurchasePreference(whatsappProduct : InAppProductInfo) {
    whatsappPreference.setSummary(getString(R.string.purchase_notification_desc) + " " + whatsappProduct.price + "!")
    whatsappPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.whatsapp_notifications_pref_title)
          .setMessage(getString(R.string.purchase_notification_confirm_whatsapp) + " " + whatsappProduct.price + "?")
          .setPositiveButton("Buy Now!", new DialogInterface.OnClickListener() {
          override def onClick(dialog : DialogInterface, which : Int) {
            val purchaseIntent = TriggrService.getPurchaseIntent(TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS)
            if (purchaseIntent.isDefined) {
              startIntentSenderForResult(purchaseIntent.get.getIntentSender,
                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0))
            }
          }
        })
          .setNegativeButton("Cancel", null)
          .show()

        false
      }
    })
  }

  private def enableWhatsappPreference() {
    whatsappPreference.setEnabled(true)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      whatsappPreference.asInstanceOf[SwitchPreference].setChecked(true)
    else
      whatsappPreference.asInstanceOf[CheckBoxPreference].setChecked(true)
    whatsappPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    whatsappPreference.setOnPreferenceChangeListener(null)
  }

  private def disableWhatsappPurchasePreference() {
    whatsappPreference.setEnabled(false)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      whatsappPreference.asInstanceOf[SwitchPreference].setChecked(false)
    else
      whatsappPreference.asInstanceOf[CheckBoxPreference].setChecked(false)
  }

  private def enableSnapchatPurchasePreference(snapchatProduct : InAppProductInfo) {
    snapchatPreference.setSummary(getString(R.string.purchase_notification_desc) + " " + snapchatProduct.price + "!")
    snapchatPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.snapchat_notifications_pref_title)
          .setMessage(getString(R.string.purchase_notification_confirm_snapchat) + " " + snapchatProduct.price + "?")
          .setPositiveButton("Buy Now!", new DialogInterface.OnClickListener() {
          override def onClick(dialog : DialogInterface, which : Int) {
            val purchaseIntent = TriggrService.getPurchaseIntent(TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS)
            if (purchaseIntent.isDefined) {
              startIntentSenderForResult(purchaseIntent.get.getIntentSender,
                1001, new Intent(), Integer.valueOf(0), Integer.valueOf(0), Integer.valueOf(0))
            }
          }
        })
          .setNegativeButton("Cancel", null)
          .show()

        false
      }
    })
  }

  private def enableSnapchatPreference() {
    snapchatPreference.setEnabled(true)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      snapchatPreference.asInstanceOf[SwitchPreference].setChecked(true)
    else
      snapchatPreference.asInstanceOf[CheckBoxPreference].setChecked(true)
    snapchatPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    snapchatPreference.setOnPreferenceChangeListener(null)
  }

  private def disableSnapchatPurchasePreference() {
    snapchatPreference.setEnabled(false)
    if(Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD_MR1)
      snapchatPreference.asInstanceOf[SwitchPreference].setChecked(false)
    else
      snapchatPreference.asInstanceOf[CheckBoxPreference].setChecked(false)
  }

  override def onActivityResult(requestCode : Int, resultCode : Int, data : Intent) {
    Log.d(tag, "Received activity result code: " + resultCode)
    Log.d(tag, "Received activity result data: " + data.getDataString)

    if(resultCode == Activity.RESULT_OK) {
      // Purchase made
      val purchaseInfo = new InAppPurchaseInfo().deserialize(data.getStringExtra("INAPP_PURCHASE_DATA"))
      Log.d("co.fether.triggr.SettingsActivity", "Purchase Info: " + purchaseInfo)
      Log.d("co.fether.triggr.SettingsActivity", "Purchase Info: " + purchaseInfo.productId)
      purchaseInfo.productId match {
        case TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS => {
          enableWhatsappPreference()
          EventActor ! EventActor.WhatsAppMessage(getString(R.string.purchase_whatsapp_notifications_desktop_message), "")
        }
        case TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS => {
          enableSnapchatPreference()
          EventActor ! EventActor.SnapchatMessage(getString(R.string.purchase_snapchat_notifications_desktop_message), "")
        }
      }
    }
  }
}
