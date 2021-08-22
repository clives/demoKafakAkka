import akka.kafka.{ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.testkit.KafkaTestkitTestcontainersSettings
import akka.kafka.testkit.scaladsl.{ScalatestKafkaSpec, TestcontainersKafkaPerClassLike}
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.scaladsl.StreamTestKit.assertAllStagesStopped
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.config.TopicConfig
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike
import akka.actor.Actor
import akka.actor.Props
import akka.event.Logging
import akka.actor.ActorSystem
import demo.models.{JsonDeserializer, JsonSerializer}
import demo.models.Protocol.DeviceReading
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.concurrent.Future

class TestcontainersProducerSpec extends SpecBase with TestcontainersKafkaPerClassLike {

  override val testcontainersSettings = KafkaTestkitTestcontainersSettings(system)
    .withNumBrokers(3)
    .withInternalTopicsReplicationFactor(2)
    .withConfigureKafka { brokerContainers =>
      brokerContainers.foreach(_.withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false"))
    }

  "EventsProducer" should {
    "should send at least 20 msg in 30second" in assertAllStagesStopped {
      val totalMessages = 20
      val partitions = 1

      // TODO: This is probably not necessary anymore since the testcontainer setup blocks until all brokers are online.
      // TODO: However it is nice reassurance to hear from Kafka itself that the cluster is formed.
      waitUntilCluster() {
        _.nodes().get().size == testcontainersSettings.numBrokers
      }

      val topic = createTopic(0, partitions, replication = 3, Map(
        // require at least two replicas be in sync before acknowledging produced record
        TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG -> "2"
      ))
      val groupId = createGroupId(0)

      val consumerConfig = consumerDefaults(new StringDeserializer, new JsonDeserializer[DeviceReading])
        .withGroupId(groupId)
        .withProperty(ConsumerConfig.METADATA_MAX_AGE_CONFIG, "100") // default was 5 * 60 * 1000 (five minutes)

      val consumerMatValue: Future[Long] = Consumer.plainSource(consumerConfig, Subscriptions.topics(topic))
        .scan(0L)((c, _) => c + 1)
        .via(IntegrationTests.logReceivedMessages()(log))
        .takeWhile(count => count < totalMessages, inclusive = true)
        .runWith(Sink.last)

      waitUntilConsumerSummary(groupId) {
        case singleConsumer :: Nil => singleConsumer.assignment.topicPartitions.size == partitions
      }

      val producerConfig: ProducerSettings[String, DeviceReading] = producerDefaults(new StringSerializer, new JsonSerializer[DeviceReading]).withProperties(
        // require acknowledgement from at least min in sync replicas (2).  default is 1
        ProducerConfig.ACKS_CONFIG -> "all"
      )

      val system = ActorSystem("EventsProducers")
      val producer= system.actorOf(EventsProducers.props(5,  Some(producerConfig), topic), "producer")

      implicit  val patienceConfig =
        PatienceConfig(timeout = Span(30, Seconds), interval = Span(5, Millis))

      // wait for consumer to consume all up until totalMessages, or timeout
      val actualCount = consumerMatValue.futureValue
      log.info("Actual messages received [{}], total messages sent [{}]", actualCount, totalMessages)
      assert(actualCount >= totalMessages)
    }
  }
}