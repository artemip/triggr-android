package co.fether.triggrtrial.json

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

    this.icon_uri = tryExtract[String](parsedJSON \ "icon_uri", "")
    this.title = tryExtract[String](parsedJSON \ "title", "")
    this.subtitle = tryExtract[String](parsedJSON \ "subtitle", "")
    this.description = tryExtract[String](parsedJSON \ "description", "")

    this
  }
}
