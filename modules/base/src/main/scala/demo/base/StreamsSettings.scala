package demo.base

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings}
import com.typesafe.config._
import demo.models.Protocol.DeviceReading
import demo.models.{JsonDeserializer, JsonSerializer}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

object StreamsSettings {
  val config = ConfigFactory.load().getConfig("kafka")

  def createConsumerSettings(system : ActorSystem): ConsumerSettings[String,DeviceReading] = {
  	ConsumerSettings(system, new StringDeserializer, new JsonDeserializer[DeviceReading])
      .withBootstrapServers(config.getString("bootstrap.servers"))
      .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")
      .withGroupId(config.getString("group.id"))
      .withClientId(config.getString("client.id"))
  }

  def createProducerSettings(system : ActorSystem): ProducerSettings[String, DeviceReading] = {
  	ProducerSettings(system, new StringSerializer, new JsonSerializer[DeviceReading])
      .withBootstrapServers(config.getString("bootstrap.servers"))
  }
}
