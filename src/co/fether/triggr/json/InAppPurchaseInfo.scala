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

    this.orderId = tryExtractString(parsedJSON \ "orderId")
    this.packageName = tryExtractString(parsedJSON \ "packageName")
    this.productId = tryExtractString(parsedJSON \ "productId")
    this.purchaseTime = tryExtractString(parsedJSON \ "purchaseTime")
    this.purchaseState = tryExtractString(parsedJSON \ "purchaseState")
    this.developerPayload = tryExtractString(parsedJSON \ "developerPayload")
    this.purchaseToken = tryExtractString(parsedJSON \ "purchaseToken")

    this
  }
}
