package demo.actor

import akka.actor.{Actor, ActorRef, Props}
import demo.actor.Protocol.{GetState, MeasureState, StateResponse, UpdateState}
import demo.models.customTypes.Guid
import org.slf4j.{Logger, LoggerFactory}

object DeviceState {
  def props(deviceId:Guid, state:  MeasureState): Props =
    Props(new DeviceState(deviceId, state))
}

class DeviceState(deviceId:Guid, state:  MeasureState) extends Actor{
  private val log: Logger = LoggerFactory.getLogger(getClass)

  def innerState( states: List[MeasureState] ): Actor.Receive={
    case state:MeasureState=>
      log.debug(s"UpdateState:$state")
      val newStates=states.filter(_.measure != state.measure) :+ state
      context.become(innerState(newStates))
    case GetState =>
      log.debug("GetState")
      sender() ! StateResponse(deviceId, states)
  }

  def receive =innerState(List(state))
}
