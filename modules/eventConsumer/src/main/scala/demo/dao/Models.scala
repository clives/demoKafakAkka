package demo.dao

import java.util.UUID

import akka.Done
import akka.actor.ActorSystem
import akka.stream.alpakka.slick.scaladsl.{Slick, SlickSession}
import akka.stream.scaladsl._
import slick.basic.DatabaseConfig
import slick.dbio.DBIOAction
import slick.jdbc.{GetResult, JdbcProfile}

import scala.concurrent.duration._
import scala.concurrent.{Await, Future}
import slick.jdbc.PostgresProfile.api._

//  String for the rawdata as a temporary solution
case class DeviceReadingTableRow( device: String, measure:String, time: java.sql.Timestamp)
class DeviceReadingTableDefinition(tag: Tag) extends Table[(String, String, java.sql.Timestamp)](tag, "DEVICE_MEASURE") {
  def id = column[String]("DEVICE_ID")
  def measure = column[String]("MEASURE")
  def time = column[java.sql.Timestamp]("TIME")

  def pk = primaryKey("pk_a", (id, time))
  def * = (id, measure, time)
}


