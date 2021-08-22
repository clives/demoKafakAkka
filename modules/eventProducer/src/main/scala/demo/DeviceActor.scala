package demo

import java.sql.Timestamp
import java.util.Calendar

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.ProducerSettings
import demo.models.DeviceModel
import demo.models.MeasureUnit.Fahrenheit
import demo.models.Protocol.{DeviceReading, EventsToSend, SendState}
import demo.models.customTypes.Guid

import scala.concurrent.duration.DurationInt


object DeviceActor {
  def props(kafkaProducer:ActorRef, desc:DeviceModel): Props =
    Props(new DeviceActor(kafkaProducer,desc))

  case object KafkaMessageSent
  case class MsgToSend(msg: List[String])
}

class DeviceActor(afkaProducer:ActorRef,desc:DeviceModel) extends Actor {
  val r = scala.util.Random
  implicit val system = ActorSystem("DeviceActor")
  import system.dispatcher

  system.scheduler.scheduleAtFixedRate(1.seconds,3.seconds, self, SendState)

  def generateEvent(): DeviceReading ={
    DeviceReading( desc.deviceId, r.nextFloat()*100, Fahrenheit, new Timestamp( Calendar.getInstance().getTime.getTime), 1.0f)
  }

  def receive = {
    case SendState =>
      afkaProducer ! EventsToSend(List(generateEvent()))
  }
}
