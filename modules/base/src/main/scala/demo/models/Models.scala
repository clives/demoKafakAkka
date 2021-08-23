package demo.models
import java.sql.Timestamp
import java.util
import java.util.UUID

import demo.models.customTypes.Guid
import org.apache.kafka.common.serialization.{Deserializer, Serializer, StringDeserializer, StringSerializer}
import play.api.libs.json.{Json, Reads, Writes}


object customTypes {
  type Guid = UUID
}

object MeasureUnit extends Enumeration {
  type MeasureUnit = Value
  val Fahrenheit, Humidity = Value
}

case class DeviceModel(deviceId: Guid, name: String, createdAt: java.util.Date)

object Protocol{

  import play.api.libs.json._

  val rds: Reads[Timestamp] = (__ \ "time").read[Long].map { long => new Timestamp(long) }
  val wrs: Writes[Timestamp] = (__ \ "time").write[Long].contramap { (a: Timestamp) => a.getTime }
  implicit val timestampFormat: Format[Timestamp] = Format(rds, wrs)

  case class EventsToSend(events: List[DeviceReading])
  case class DeviceReading( deviceId: Guid, currentValue: Float, unit: MeasureUnit.Value, timeStamp: Timestamp, version: Float)
  case object SendState
  implicit val MeasureUnitFrmt: Format[MeasureUnit.Value] = Json.formatEnum(MeasureUnit)
  implicit val DeviceReadingFrmt: Format[DeviceReading] = Json.format[DeviceReading]
}

class JsonDeserializer[A: Reads] extends Deserializer[A] {

  private val stringDeserializer = new StringDeserializer

  override def configure(configs: util.Map[String, _], isKey: Boolean) =
    stringDeserializer.configure(configs, isKey)

  override def deserialize(topic: String, data: Array[Byte]) =
    Json.parse(stringDeserializer.deserialize(topic, data)).as[A]

  override def close() =
    stringDeserializer.close()

}

class JsonSerializer[A: Writes] extends Serializer[A] {

  private val stringSerializer = new StringSerializer

  override def configure(configs: util.Map[String, _], isKey: Boolean) =
    stringSerializer.configure(configs, isKey)

  override def serialize(topic: String, data: A) =
    stringSerializer.serialize(topic, Json.stringify(Json.toJson(data)))

  override def close() =
    stringSerializer.close()

}