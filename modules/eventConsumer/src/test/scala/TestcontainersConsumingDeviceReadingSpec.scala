import java.sql.Timestamp
import java.util.{Calendar, UUID}

import akka.kafka.Subscriptions
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.testkit.KafkaTestkitTestcontainersSettings
import akka.kafka.testkit.scaladsl.{ScalatestKafkaSpec, TestcontainersKafkaPerClassLike}
import akka.stream.alpakka.slick.scaladsl.SlickSession
import akka.stream.scaladsl.{Sink, Source}
import akka.stream.testkit.scaladsl.StreamTestKit.assertAllStagesStopped
import demo.dao.DeviceReadingTableRow
import demo.models.Protocol.DeviceReading
import demo.models.{JsonDeserializer, JsonSerializer, MeasureUnit}
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.{ProducerConfig, ProducerRecord}
import org.apache.kafka.common.config.TopicConfig
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, shortstacks}
import org.scalatest.concurrent.{Eventually, ScalaFutures}
import org.scalatest.matchers.should.Matchers
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.wordspec.AnyWordSpecLike
import slick.jdbc.GetResult

import scala.concurrent.Future
import slick.jdbc.H2Profile.api._
import slick.sql.SqlStreamingAction


class TestcontainersConsumingDeviceReadingSpec extends SpecBase with TestcontainersKafkaPerClassLike  with BeforeAndAfterEach{

  implicit val session: SlickSession = SlickSession.forConfig("slick-h2")
  import session.profile.api._

  implicit val getDeviceReadingResult = GetResult(r => DeviceReadingTableRow(r.nextString, r.nextString, r.nextTimestamp()))
  val createTable = sqlu"""CREATE TABLE DEVICE_MEASURE(DEVICE_ID VARCHAR(40), MEASURE VARCHAR(200), TIME TIMESTAMP)"""
  val dropTable = sqlu"""DROP TABLE DEVICE_MEASURE"""
  val selectAllUsers = sql"SELECT DEVICE_ID, MEASURE, TIME FROM DEVICE_MEASURE".as[DeviceReadingTableRow]
  implicit  override val patienceConfig =
    PatienceConfig(timeout = Span(30, Seconds), interval = Span(5, Millis))

  override def beforeEach(): Unit = session.db.run(createTable).futureValue
  override def afterEach(): Unit = session.db.run(dropTable).futureValue

  override def afterAll(): Unit = {
    system.registerOnTermination(() => session.close())
  }

  override val testcontainersSettings = KafkaTestkitTestcontainersSettings(system)
    .withNumBrokers(3)
    .withInternalTopicsReplicationFactor(2)
    .withConfigureKafka { brokerContainers =>
      brokerContainers.foreach(_.withEnv("KAFKA_AUTO_CREATE_TOPICS_ENABLE", "false"))
    }

  "EventsComsumer" should {
    "consume and save the msg into h2" in assertAllStagesStopped {
      val totalMessages = 100L
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

      EventsConsumer.consumeAndCommit(consumerConfig,Subscriptions.topics(topic))

      waitUntilConsumerSummary(groupId) {
        case singleConsumer :: Nil => singleConsumer.assignment.topicPartitions.size == partitions
      }

      val producerConfig = producerDefaults(new StringSerializer, new JsonSerializer[DeviceReading]).withProperties(
        // require acknowledgement from at least min in sync replicas (2).  default is 1
        ProducerConfig.ACKS_CONFIG -> "all"
      )

      val devicesUUID = List.tabulate(5)( _=>UUID.randomUUID())
      val r = scala.util.Random

      val result = Source(0L to totalMessages)
        .via(IntegrationTests.logSentMessages()(log))
        .map { number =>
          if (number == totalMessages / 2) {
            IntegrationTests.stopRandomBroker(brokerContainers, number)(log)
          }
          number
        }
        .map{number =>
          val deviceIndex = number % devicesUUID.size
          val deviceUUID = devicesUUID(deviceIndex.toInt)
          new ProducerRecord(topic, partition0, DefaultKey,
          DeviceReading(deviceUUID, r.nextFloat()*deviceIndex, MeasureUnit.Fahrenheit,
            new  Timestamp(Calendar.getInstance().getTime.getTime),1.0f ))}
        .runWith(Producer.plainSink(producerConfig))

      result.futureValue

      //todo - check db content, if size < totalMsg , wait and retry
      Thread.sleep(10000)
      val dbResultFuture=session.db.run(selectAllUsers)
      val dbResult =dbResultFuture.futureValue
      log.info(s"Nbr of events in H2:${dbResult.size}")
      assert(dbResult.size >= totalMessages)
    }
  }
}