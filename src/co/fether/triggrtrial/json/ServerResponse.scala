package co.fether.triggrtrial.json

import net.liftweb.json._
import net.liftweb.json.JsonDSL._

/*
 * 'server_response' : {
 *   'status' : {"ok", "error"},
 *   'message' : message,
 *   'paired_device_id' : paired_device_id
 * }
 */
class ServerResponse ( var status : String = "", var message : String = "", var paired_device_id : String = "") extends JsonObject {
  def serialize() = {
    val jsonMap =
      ("status" -> this.status) ~
        ("message" -> this.message) ~
        ("paired_device_id" -> this.paired_device_id)

    compact(render(jsonMap))
  }

  def deserialize( json : String ) = {
    val parsedJSON = parse(json)

    this.status = tryExtractString(parsedJSON \ "status")
    this.message = tryExtractString(parsedJSON \ "message")
    this.paired_device_id = tryExtractString(parsedJSON \ "paired_device_id")

    this
  }
}
