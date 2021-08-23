package demo.actor

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import demo.actor.Protocol._
import demo.models.customTypes.Guid
import org.slf4j.{Logger, LoggerFactory}

import scala.language.postfixOps

object DevicesStateManagerActor {
  def props(): Props =
    Props(new DevicesStateManagerActor())
}

class DevicesStateManagerActor() extends Actor {

  private val log: Logger = LoggerFactory.getLogger(getClass)
  implicit val system = ActorSystem("DevicesStateManagerActor")

  private def createDeviceState(device: Guid, state: MeasureState): ActorRef ={
    implicit val system = ActorSystem("DevicesStateActor")
    system.actorOf(DeviceStateActor.props(device, state ))
  }

  def state(devices :List[DeviceActor], currentState: Map[Guid ,List[MeasureState]]=Map.empty, expectedDeviceResponses :Option[Int]=None, requester:Option[ActorRef]=None  ): Actor.Receive={
    case StateResponse(deviceId,deviceStates)=>
      log.debug(s"update from device:$deviceId states size:${currentState.size}")
      val newCurrentState=currentState ++ Map(deviceId->deviceStates)
      if( newCurrentState.size >=  expectedDeviceResponses.getOrElse(0)){
        requester.foreach( _ ! newCurrentState)
        context.become(state(devices, Map.empty))
      }else{
        context.become(state(devices, newCurrentState,expectedDeviceResponses,requester))
      }
    case GetAllStates =>
      log.debug(s"GetAllStates - nbr of devices:${devices.size}")
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