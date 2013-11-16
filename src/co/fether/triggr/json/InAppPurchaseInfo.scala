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

    this.orderId = (parsedJSON \ "orderId").extractOrElse[String]("")
    this.packageName -> (parsedJSON \ "packageName").extractOrElse[String]("")
    this.productId -> (parsedJSON \ "productId").extractOrElse[String]("")
    this.purchaseTime -> (parsedJSON \ "purchaseTime").extractOrElse[String]("")
    this.purchaseState -> (parsedJSON \ "purchaseState").extractOrElse[String]("")
    this.developerPayload -> (parsedJSON \ "developerPayload").extractOrElse[String]("")
    this.purchaseToken -> (parsedJSON \ "purchaseToken").extractOrElse[String]("")

    this
  }
}
