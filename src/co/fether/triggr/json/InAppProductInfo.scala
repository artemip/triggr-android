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

    this.productId = tryExtractString(parsedJSON \ "productId")
    this.`type` = tryExtractString(parsedJSON \ "type")
    this.price = tryExtractString(parsedJSON \ "price")
    this.title = tryExtractString(parsedJSON \ "title")
    this.description = tryExtractString(parsedJSON \ "description")

    this
  }
}
