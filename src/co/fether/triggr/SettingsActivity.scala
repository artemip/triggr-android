package co.fether.triggr

import android.preference.{SwitchPreference, Preference, PreferenceActivity}
import android.os.Bundle
import android.preference.Preference.OnPreferenceChangeListener
import android.content.{Intent, DialogInterface}
import android.app.AlertDialog.Builder
import co.fether.triggr.json.{InAppProductInfo, InAppPurchaseInfo}

class SettingsActivity extends PreferenceActivity {
  var whatsappPreference : SwitchPreference = null
  var snapchatPreference : SwitchPreference = null

  override def onCreate(savedInstanceState : Bundle) {
    super.onCreate(savedInstanceState)
    addPreferencesFromResource(R.xml.preferences)

    whatsappPreference = findPreference(Preferences.PREF_WHATSAPP_NOTIFICATIONS).asInstanceOf[SwitchPreference]
    snapchatPreference = findPreference(Preferences.PREF_SNAPCHAT_NOTIFICATIONS).asInstanceOf[SwitchPreference]

    val allProducts =
      TriggrService.getProductInfo(
        List(TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS,
          TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS))
    val purchasedProducts = TriggrService.getPurchasedProducts

    if (!purchasedProducts.exists(pid => pid == TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS)) {
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

    if (!purchasedProducts.exists(pid => pid == TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS)) {
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

  private def enableWhatsappPurchasePreference(whatsappProduct : InAppProductInfo) {
    whatsappPreference.setSummary(R.string.purchase_notification_desc + whatsappProduct.price + "!")
    whatsappPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.whatsapp_notifications_pref_title)
          .setMessage(R.string.purchase_notification_confirm_whatsapp + whatsappProduct.price + "?")
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
    whatsappPreference.setChecked(true)
    whatsappPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    whatsappPreference.setOnPreferenceChangeListener(null)
  }

  private def disableWhatsappPurchasePreference() {
    whatsappPreference.setEnabled(false)
    whatsappPreference.setChecked(false)
  }

  private def enableSnapchatPurchasePreference(snapchatProduct : InAppProductInfo) {
    snapchatPreference.setSummary(R.string.purchase_notification_desc + snapchatProduct.price + "!")
    snapchatPreference.setOnPreferenceChangeListener(new OnPreferenceChangeListener {
      def onPreferenceChange(p1: Preference, p2: scala.Any): Boolean = {
        new Builder(SettingsActivity.this)
          .setIcon(android.R.drawable.ic_dialog_info)
          .setTitle(R.string.snapchat_notifications_pref_title)
          .setMessage(R.string.purchase_notification_confirm_snapchat + snapchatProduct.price + "?")
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
    snapchatPreference.setChecked(true)
    snapchatPreference.setSummary(R.string.whatsapp_notifications_pref_desc)
    snapchatPreference.setOnPreferenceChangeListener(null)
  }

  private def disableSnapchatPurchasePreference() {
    snapchatPreference.setEnabled(false)
    snapchatPreference.setChecked(false)
  }

  override def onActivityResult(requestCode : Int, resultCode : Int, data : Intent) {
    if(data.getIntExtra("RESPONSE_CODE", 0) == 1) {
      // Purchase made
      val purchaseInfo = new InAppPurchaseInfo().deserialize(data.getStringExtra("INAPP_PURCHASE_DATA"))

      purchaseInfo.productId match {
        case TriggrService.PROD_ID_WHATSAPP_NOTIFICATIONS => {
          enableWhatsappPreference()
        }
        case TriggrService.PROD_ID_SNAPCHAT_NOTIFICATIONS => {
          enableSnapchatPreference()
        }
      }
    }
  }
}
