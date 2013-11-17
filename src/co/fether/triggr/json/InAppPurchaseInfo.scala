package co.fether.triggr.json

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

class InAppPurchaseInfo(
                         var orderId : String = "",
                         var packageName : String = "",
                         var productId : String = "",
                         var purchaseTime : String = "",
                         var purchaseState : String = "",
                         var developerPayload : String = "",
                         var purchaseToken : String = "") extends JsonObject {

  implicit override val formats = Serialization.formats(NoTypeHints)

  def serialize() = {
    val jsonMap =
      ("orderId" -> this.orderId) ~
        ("packageName" -> this.packageName) ~
        ("productId" -> this.productId) ~
        ("purchaseTime" -> this.purchaseTime) ~
        ("purchaseState" -> this.purchaseState) ~
        ("developerPayload" -> this.developerPayload) ~
        ("purchaseToken" -> this.purchaseToken)

    compact(render(jsonMap))
  }

  def deserialize( json : String ) = {
    val parsedJSON = parse(json)

    this.orderId = (parsedJSON \ "orderId").extract[String]
    this.packageName = (parsedJSON \ "packageName").extract[String]
    this.productId = (parsedJSON \ "productId").extract[String]
    this.purchaseTime = (parsedJSON \ "purchaseTime").extract[String]
    this.purchaseState = (parsedJSON \ "purchaseState").extract[String]
    this.developerPayload = (parsedJSON \ "developerPayload").extractOrElse[String]("")
    this.purchaseToken = (parsedJSON \ "purchaseToken").extract[String]

    this
  }
}
