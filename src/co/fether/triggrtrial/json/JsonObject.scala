package co.fether.triggrtrial.json

import net.liftweb.json._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.MappingException

abstract class JsonObject {
  implicit val formats = Serialization.formats(NoTypeHints)

  def serialize() : String
  def deserialize( json : String ) : JsonObject

  protected def tryExtract[T](value: JValue, default: T): T = {
    try {
      value.extract[T]
    } catch {
      case e: MappingException => default
      case e: Exception => throw e
    }
  }
}
