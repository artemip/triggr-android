package co.fether.triggr.json

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/*
 * 'notification' : {
 *   'icon_uri' : icon_uri,
 *   'title' : title,
 *   'subtitle' : subtitle,
 *   'description' : description
 * }
 */
class Notification( var icon_uri : String = "", var title : String = "", var subtitle : String = "", var description : String = "" ) extends JsonObject {
  def serialize() = {
    val jsonMap =
      ("icon_uri" -> this.icon_uri) ~
        ("title" -> this.title) ~
        ("subtitle" -> this.subtitle) ~
        ("description" -> this.description)

    compact(render(jsonMap))
  }

  def deserialize( json : String ) = {
    val parsedJSON = parse(json)

    this.icon_uri = (parsedJSON \ "icon_uri").extractOrElse[String]("")
    this.title = (parsedJSON \ "title").extractOrElse[String]("")
    this.subtitle = (parsedJSON \ "subtitle").extractOrElse[String]("")
    this.description = (parsedJSON \ "description").extractOrElse[String]("")

    this
  }
}
