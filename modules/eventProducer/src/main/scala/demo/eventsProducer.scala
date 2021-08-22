import java.util.{Calendar, UUID}
import EventsProducers.KafkaMessageSent
import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.{ProducerMessage, ProducerSettings}
import demo.DeviceActor
import demo.base.StreamsSettings
import demo.models.DeviceModel
import demo.models.Protocol.{DeviceReading, EventsToSend}
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}
import scala.collection.immutable.Seq
import scala.language.postfixOps
import scala.util.{Failure, Success}
import akka.kafka.scaladsl.SendProducer

object EventsProducers {
  def props(nbrOfActors: Int, producerSettingOpt: Option[ProducerSettings[String, DeviceReading]], topic: String): Props =
    Props(new EventsProducers(nbrOfActors,producerSettingOpt, topic))

  case object KafkaMessageSent
}


class EventsProducers(nbrOfDevicesToCreate:Int, producerSettingOpt: Option[ProducerSettings[String, DeviceReading]],topic: String ) extends Actor {

  val devices = createAndStartDevices()
  private val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("EventsProducersActor")
  import system.dispatcher
  val producerSettings = producerSettingOpt.getOrElse(StreamsSettings.createProducerSettings(system))
  val producer = SendProducer(producerSettings)

  private def createAndStartDevices(): Seq[ActorRef] ={
    implicit val system = ActorSystem("DevicesActor")
    List.tabulate(nbrOfDevicesToCreate){
      index =>
        DeviceModel(UUID.randomUUID(), s"Device$index", Calendar.getInstance().getTime)
    }.map { device =>
      system.actorOf(DeviceActor.props(self, device ), device.name)
    }
  }

  def receivingMsg(waitingMsg :List[DeviceReading]): Actor.Receive={
    case msg: EventsToSend =>
      context.become(sendingMsg(List.empty))
      sendEvents(msg.events  ++ waitingMsg)
  }

  def sendingMsg(waitingMsg :List[DeviceReading]) : Actor.Receive= {
    case msg: EventsToSend =>
      context.become(sendingMsg(waitingMsg++msg.events))
    case KafkaMessageSent =>
      context.become(receivingMsg(List.empty))
      if(waitingMsg.nonEmpty) self ! EventsToSend(waitingMsg)
  }

  private def sendEvents(listEvents :List[DeviceReading])={
    try {
      val msgs= listEvents.map{
        word => new ProducerRecord(topic, word.deviceId.toString, word)
      }
      val envelope: ProducerMessage.Envelope[String, DeviceReading, String] =
        ProducerMessage.multi(msgs, "context")
      val send  = producer.sendEnvelope(envelope)
      send.onComplete{
        case Failure(error) =>
          log.error("Failure",error)
        case Success(result) =>
          log.debug(s"Success $result")
          self ! KafkaMessageSent
      }
    }catch {
      case error: Throwable =>
        log.error("Failure", error)
        self ! EventsToSend(listEvents)
        self ! KafkaMessageSent
    }
  }

  override def receive: Receive = receivingMsg(List.empty)
}