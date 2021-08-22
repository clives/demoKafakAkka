package demo.actor

import java.util.{Calendar, UUID}

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.kafka.ProducerMessage
import akka.kafka.scaladsl.SendProducer
import demo.actor.Protocol.{DeviceActor, GetAllStates, GetState, MeasureState, NewMeasure, UpdateDeviceState}
import demo.base.StreamsSettings
import demo.models.DeviceModel
import demo.models.Protocol.{DeviceReading, DeviceReadingFrmt, EventsToSend}
import demo.models.customTypes.Guid
import org.apache.kafka.clients.producer.ProducerRecord
import org.slf4j.{Logger, LoggerFactory}

import scala.collection.immutable.Seq
import scala.language.postfixOps
import scala.util.{Failure, Success}

object DevicesStateManager {
  def props(): Props =
    Props(new DevicesStateManager())
}

class DevicesStateManager() extends Actor {

  private val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("DevicesStateManagerActor")
  import system.dispatcher

  private def createDeviceState(device: Guid, state: MeasureState): ActorRef ={
    implicit val system = ActorSystem("DevicesStateActor")
    system.actorOf(DeviceState.props(device, state ))
  }

  def state(devices :List[DeviceActor], currentState: Map[Guid ,MeasureState]=Map.empty, expectedDeviceResponses :Option[Int]=None, requester:Option[ActorRef]=None  ): Actor.Receive={
    case UpdateDeviceState(deviceId,deviceState)=>
      val newCurrentState=currentState ++ Map(deviceId->deviceState)
      if( newCurrentState.size >=  expectedDeviceResponses.getOrElse(0)){
        requester.foreach( _ ! newCurrentState)
        context.become(state(devices, Map.empty))
      }else{
        context.become(state(devices, newCurrentState,expectedDeviceResponses,requester))
      }
    case GetAllStates =>
      context.become(state(devices, Map.empty, expectedDeviceResponses=Some(devices.size), Some(sender())))
      devices.foreach( _.actorRef ! GetState)
    case NewMeasure(deviceReading) =>
      log.debug(s"NewMeasure for device:${deviceReading.deviceId}")
      val deviceId = deviceReading.deviceId
      val deviceState =MeasureState(deviceReading.currentValue,deviceReading.unit, deviceReading.timeStamp)
      val device = if( !devices.map(_.deviceId).contains(deviceId)){
        log.debug(s"c $deviceId")
        val newDevice=createDeviceState(deviceId, deviceState)
        context.become(state(devices :+ DeviceActor(deviceId,newDevice)))
      }else{
        log.debug(s"updateDevice $deviceId")
        devices.find(_.deviceId == deviceId).foreach(_.actorRef ! deviceState)
      }
  }

  override def receive: Receive = state(List.empty)
}