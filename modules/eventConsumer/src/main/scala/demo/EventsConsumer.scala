import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.kafka.ConsumerMessage.CommittableMessage
import akka.kafka.{CommitterSettings, ConsumerSettings, Subscription, Subscriptions}
import akka.kafka.scaladsl.{Committer, Consumer}
import akka.kafka.scaladsl.Consumer.DrainingControl
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.{ActorMaterializer, FlowShape, Graph}
import akka.stream.scaladsl.Sink
import demo.models.Protocol.DeviceReading
import play.api.libs.json._

import scala.collection.immutable.Seq
import scala.concurrent.Future
import org.slf4j.{Logger, LoggerFactory}
import slick.jdbc.H2Profile.api._
import java.sql.Timestamp

import demo.actor.DevicesStateManager
import demo.actor.Protocol.{MeasureState, NewMeasure, UpdateDeviceState}

object EventsConsumer {
  implicit val system = ActorSystem("EventsConsumer")
  import system.dispatcher
  private val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val session = SlickSession.forConfig("slick-h2")
  val stateManager= system.actorOf(DevicesStateManager.props(), "DevicesStateManager")
  val committerSettings = CommitterSettings(system)

  def consumeAndCommit[K, V, O](consumerSettings: ConsumerSettings[String, DeviceReading], subscription: Subscription)= {
    val consumerMatValue: Future[Done] = Consumer
        .committableSource(consumerSettings, subscription)
        .mapAsync(1) { msg =>
          buildDeviceReadingRow(msg.record.key, msg.record.value)
        }
      .via(
        Slick.flow(deviceReading => sqlu"INSERT INTO DEVICE_MEASURE VALUES(${deviceReading._1},${deviceReading._2},${deviceReading._3})")
      )
      .log("nr-of-updated-rows")
      .runWith(Sink.ignore)
    consumerMatValue
  }

  def buildDeviceReadingRow(key: String, event: DeviceReading): Future[(String,String,Timestamp)] = {
    stateManager ! NewMeasure(event)
    Future.successful((event.deviceId.toString, Json.toJson(event).toString(), event.timeStamp))
  }
}