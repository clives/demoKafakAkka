package demo.actor

import java.sql.Timestamp

import akka.actor.ActorRef
import demo.models.MeasureUnit.MeasureUnit
import demo.models.Protocol.DeviceReading
import demo.models.customTypes.Guid

object Protocol {
  case class MeasureState(measure: Float, unit: MeasureUnit, time: Timestamp)
  case object GetState
  case class UpdateState(state:MeasureState)
  case class UpdateDeviceState(deviceId: Guid,state:MeasureState)
  case class NewMeasure(event: DeviceReading)
  case class DeviceActor(deviceId: Guid, actorRef: ActorRef)
  case object GetAllStates
  case class DevicesMeasureState( deviceStateMap: Map[Guid,MeasureState])
  //case class GetAverageState(timeperiodMs: long)

  // assume multiple measure for same device with different MeasureUnit
  case class StateResponse(deviceId:Guid, states: List[MeasureState])
}
