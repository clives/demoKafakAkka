package demo.dao

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


