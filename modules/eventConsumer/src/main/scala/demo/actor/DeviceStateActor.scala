package demo.actor

import akka.actor.{Actor, Props}
import demo.actor.Protocol.{GetState, MeasureState, StateResponse}
import demo.models.customTypes.Guid
import org.slf4j.{Logger, LoggerFactory}

object DeviceStateActor {
  def props(deviceId:Guid, state:  MeasureState): Props =
    Props(new DeviceStateActor(deviceId, state))
}

class DeviceStateActor(deviceId:Guid, state:  MeasureState) extends Actor{
  private val log: Logger = LoggerFactory.getLogger(getClass)

  def innerState( states: List[MeasureState] ): Actor.Receive={
    case state:MeasureState=>
      log.debug(s"UpdateState:$state")
      val newStates=states.filter(_.unit != state.unit) :+ state
      context.become(innerState(newStates))
    case GetState =>
      log.debug("GetState")
      sender() ! StateResponse(deviceId, states)
  }

  def receive =innerState(List(state))
}
