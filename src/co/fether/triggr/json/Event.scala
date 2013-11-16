package co.fether.triggr.json

import co.fether.triggr.Preferences
import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/*
 * 'event' : {
 *   'sender_id' : sender_id,
 *   'type' : type,
 *   'notification' : {
 *     'icon_uri' : icon_uri,
 *     'title' : title,
 *     'subtitle' : subtitle,
 *     'description' : description
 *   },
 *   'handlers' : ['notify', 'lower_volume', 'restore_volume', 'alert_noise']
 * }
 */
class Event(var sender_id : String = Preferences.getDeviceId().toString, var `type` : String = "", var notification : Notification = new Notification(), var handlers : List[String] = List("none")) extends JsonObject {
  def serialize(): String = {
    val jsonMap =
      ("sender_id" -> this.sender_id) ~
      ("type" -> this.`type`) ~
      ("notification" -> parse(this.notification.serialize())) ~
      ("handlers" -> this.handlers)

    compact(render(jsonMap))
  }

  def deserialize(json: String) = {
    val parsedJSON = parse(json)

    this.sender_id = (parsedJSON \ "sender_id").extractOrElse[String]("")
    this.`type` = (parsedJSON \ "type").extractOrElse[String]("")
    this.notification = new Notification().deserialize(compact(render(parsedJSON \ "notification")))
    this.handlers = (parsedJSON \ "handlers").extractOrElse[List[String]](List("none"))

    this
  }
}
