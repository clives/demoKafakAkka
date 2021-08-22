import Dependencies._


libraryDependencies ++= Seq(
  logbackClassic,
  logbackCore,
  logstashLogback,
  akkaStream,
  akkaStreamKafka,
  akkaHttpCore,
  akkaHttp,
  akkaTestkit,
  akkaStreamsTestKit,
  akkaStreamKafkaTestkit
)