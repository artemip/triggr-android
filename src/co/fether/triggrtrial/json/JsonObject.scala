package co.fether.triggrtrial.json

import net.liftweb.json._
import net.liftweb.json.JsonAST.JValue
import net.liftweb.json.MappingException

abstract class JsonObject {
  implicit val formats = Serialization.formats(NoTypeHints)

  def serialize() : String
  def deserialize( json : String ) : JsonObject

  protected def tryExtractString(value: JValue, default: String = ""): String = {
    try {
      value.extract[String]
    } catch {
      case e: MappingException => default
      case e: Exception => throw e
    }
  }

  protected def tryExtractList(value: JValue, default: List[String] = List[String]()): List[String] = {
    try {
      value.extract[List[String]]
    } catch {
      case e: MappingException => default
      case e: Exception => throw e
    }
  }
}
