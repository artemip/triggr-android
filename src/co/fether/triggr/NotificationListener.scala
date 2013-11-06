package co.fether.triggr

import android.accessibilityservice.{AccessibilityServiceInfo, AccessibilityService}
import android.view.accessibility.AccessibilityEvent
import android.app.Notification
import scala.collection.mutable
import android.util.Log
import java.util
import co.fether.triggr.EventActor.{SnapChatMessage, WhatsAppMessage}
import android.telephony.PhoneNumberUtils

class TriggrNotificationListener extends AccessibilityService {

  case class NotificationEvent(title : String, description : String)

  override def onServiceConnected() {
    val info = new AccessibilityServiceInfo()
    info.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED
    info.packageNames = List("com.whatsapp", "com.snapchat.android").toArray
    info.feedbackType = AccessibilityServiceInfo.FEEDBACK_ALL_MASK
    this.setServiceInfo(info)
  }

  override def onAccessibilityEvent(event: AccessibilityEvent) {
    event.getPackageName match {
      case "com.whatsapp" => {
        val notification = getNotificationEvent(event)

        notification match {
          case Some(n) => EventActor ! WhatsAppMessage(PhoneNumberUtils.formatNumber(n.title), n.description)
          case None => EventActor ! WhatsAppMessage("New WhatsApp Message", "")
        }
      }
      case "com.snapchat.android" => {
        val notification = getNotificationEvent(event)

        notification match {
          case Some(n) => EventActor ! SnapChatMessage(n.title, n.description)
          case None => EventActor ! SnapChatMessage("New SnapChat Message", "")
        }
      }
      case _ =>
    }
  }

  private def getNotificationEvent(event : AccessibilityEvent) : Option[NotificationEvent] = {
    val notification = event.getParcelableData.asInstanceOf[Notification]
      val views = notification.contentView
      val secretClass = views.getClass

      try {
        val text = mutable.Map.empty[Integer, String]

        val outerFields = secretClass.getDeclaredFields.toList
        val outerFieldIndices = 0 until outerFields.length

        for (i <- outerFieldIndices if outerFields(i).getName.equals("mActions")) {
          outerFields(i).setAccessible(true)
          val actions = outerFields(i).get(views).asInstanceOf[util.ArrayList[Object]].toArray.toList

          for (action <- actions) {
            val innerFields = action.getClass.getDeclaredFields
            val innerFieldViewIds = action.getClass.getSuperclass.getDeclaredFields

            var value : Object = null
            var `type` : Int = -1
            var viewId : Int = -1

            for(field <- innerFields) {
              field.setAccessible(true)

              field.getName match {
                case "value" => {
                  value = field.get(action)
                }
                case "type" => {
                  `type` = field.getInt(action)
                }
                case _ =>
              }
            }

            for(field <- innerFieldViewIds) {
              field.setAccessible(true)

              field.getName match {
                case "viewId" => {
                  viewId = field.getInt(action)
                }
                case _ =>
              }
            }

            if(`type` == 9 || `type` == 10) {
              text.put(viewId, value.toString)
            }
          }
        }

        val title = text.get(16908310).getOrElse("Unknown Contact").trim
        val desc = text.get(16908358).getOrElse("New Message")

        Some(NotificationEvent(title, desc))
    } catch {
      case e : Exception => {
        val trace = Log.getStackTraceString(e)
        Log.e("TriggrNotificationListener", trace)

        None
       }
    }
  }

  override def onInterrupt() {

  }
}
