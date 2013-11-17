package co.fether.triggrtrial.json

import net.liftweb.json.{NoTypeHints, Serialization}

abstract class JsonObject {
  implicit val formats = Serialization.formats(NoTypeHints)

  def serialize() : String
  def deserialize( json : String ) : JsonObject
}
