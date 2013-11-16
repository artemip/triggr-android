package co.fether.triggr.json

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class InAppProductInfo(
                    var productId : String = "",
                    var `type` : String = "",
                    var price : String = "",
                    var title : String = "",
                    var description : String = "") extends JsonObject {

  def serialize() = {
    val jsonMap =
      ("productId" -> this.productId) ~
        ("type" -> this.`type`) ~
        ("price" -> this.price) ~
        ("title" -> this.title) ~
        ("description" -> this.description)

    compact(render(jsonMap))
  }

  def deserialize( json : String ) = {
    val parsedJSON = parse(json)

    this.productId = (parsedJSON \ "productId").extractOrElse[String]("")
    this.`type` = (parsedJSON \ "type").extractOrElse[String]("")
    this.price = (parsedJSON \ "price").extractOrElse[String]("")
    this.title = (parsedJSON \ "title").extractOrElse[String]("")
    this.description = (parsedJSON \ "description").extractOrElse[String]("")

    this
  }
}
