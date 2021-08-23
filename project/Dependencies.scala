import sbt._

object Dependencies {

  val akkaVersion = "2.5.31"
  val akkaHttpVersion = "10.0.1"
  val akkaActor = "com.typesafe.akka" %% "akka-actor" % akkaVersion
  val akkaCluster = "com.typesafe.akka" %% "akka-cluster" % akkaVersion
  val akkaClusterTools = "com.typesafe.akka" %% "akka-cluster-tools" % akkaVersion
  val akkaRemoting = "com.typesafe.akka" %% "akka-remote" % akkaVersion
  val akkaClusterSharding = "com.typesafe.akka" %% "akka-cluster-sharding" % akkaVersion
  val akkaDData = "com.typesafe.akka" %% "akka-distributed-data" % akkaVersion
  val akkaPersistence = "com.typesafe.akka" %% "akka-persistence" % akkaVersion
  val akkaStreamsTestKit = "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test
  val akkaSLF4J = "com.typesafe.akka" %% "akka-slf4j" % akkaVersion
  val akkaTestkit = "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test
  val akkaStreamKafkaTestkit ="com.typesafe.akka" %% "akka-stream-kafka-testkit" % "2.1.1" % Test
  val akkaStream = "com.typesafe.akka" %% "akka-stream" % akkaVersion
  val akkaStreamKafka = "com.typesafe.akka" %% "akka-stream-kafka" % "2.1.1"
  val akkaHttpCore = "com.typesafe.akka" %% "akka-http-core" % akkaHttpVersion
  val akkaHttp = "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
  val akkSlick =  "com.lightbend.akka" %% "akka-stream-alpakka-slick" % "3.0.3"

  val cakeVersion = "2.3.1"
  val cakeClient = "net.cakesolutions" %% "scala-kafka-client" % cakeVersion
  val cakeClientAkka  = "net.cakesolutions" %% "scala-kafka-client-akka" % cakeVersion
  val cakeclientTest = "net.cakesolutions" %% "scala-kafka-client-testkit" % cakeVersion


  val testContainersVersion = "0.39.5"
  val testContainersScala = "com.dimafeng" %% "testcontainers-scala-scalatest" % testContainersVersion % "test"
  val testContainersPostgres = "com.dimafeng" %% "testcontainers-scala-postgresql" % testContainersVersion % "test"
  val testContainersKafka = "com.dimafeng" %% "testcontainers-scala-kafka" % testContainersVersion % "test"

  val playJson = "com.typesafe.play" %% "play-json" % "2.9.2"

  val akkaManagementVersion = "1.0.5"
  val akkaManagement = "com.lightbend.akka.management" %% "akka-management" % akkaManagementVersion
  val akkaManagementClusterHTTP = "com.lightbend.akka.management" %% "akka-management-cluster-http" % akkaManagementVersion

  val alpakkaKafkaVersion = "1.0.1"
  val alpakkaKafka = "com.typesafe.akka" %% "akka-stream-kafka" % alpakkaKafkaVersion
  val alpakkaKafkaTestKit = "com.typesafe.akka" %% "akka-stream-kafka-testkit" % alpakkaKafkaVersion % Test

  val alpakkaS3 = "com.lightbend.akka" %% "akka-stream-alpakka-s3" % "1.1.2"

  val lightbendAkkaVersion = "1.1.12"
  val akkaDiagnostics = "com.lightbend.akka" %% "akka-diagnostics" % lightbendAkkaVersion
  val akkaSplitBrainResolver = "com.lightbend.akka" %% "akka-split-brain-resolver" % lightbendAkkaVersion

  val apacheCommonsCompress = "org.apache.commons" % "commons-compress" % "1.19" % "compile"

  val akkaPersistenceCassandra = "com.typesafe.akka" %% "akka-persistence-cassandra" % "0.54"

  val caffeineStandalone = "com.github.ben-manes.caffeine" % "caffeine" % "2.8.8"

  val json4sCore = "org.json4s" %% "json4s-core" % "3.6.4"
  val json4sNative = "org.json4s" %% "json4s-native" % "3.6.4"

  val kamonCore = "io.kamon" %% "kamon-core" % "1.1.5"
  val kamonDatadog = "io.kamon" %% "kamon-datadog" % "1.0.0"
  val kamonPlay = "io.kamon" %% "kamon-play-2.6" % "1.1.1"

  val libphonenumber = "com.googlecode.libphonenumber" % "libphonenumber" % "8.10.4"

  val logbackVersion = "1.2.3"
  val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion
  val logbackCore = "ch.qos.logback" % "logback-core" % logbackVersion

  val logbackTypesafeConfig = "com.tersesystems.logback" % "logback-typesafe-config" % "0.16.1"

  val julToSlf4j = "org.slf4j" % "jul-to-slf4j" % "1.7.26"
  val jclOverSlf4j = "org.slf4j" % "jcl-over-slf4j" % "1.7.26"
  val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % "1.7.26"

  val logstashLogback = "net.logstash.logback" % "logstash-logback-encoder" % "6.6"

  val maxmind = "com.maxmind.geoip" % "geoip-api" % "1.3.1"

  val mindrot = "org.mindrot" % "jbcrypt" % "0.4"

  val mockitoCore = "org.mockito" % "mockito-core" % "3.0.0" % Test

  val monixShaded = "io.monix" %% "shade" % "1.10.0"


  val phoenix = "org.apache.phoenix" % "phoenix-client" % "5.0.0.7.2.2.0-244"
  val pinotJava = "org.apache.pinot" % "pinot-java-client" % "0.5.0"
  val postgres = "org.postgresql" % "postgresql" % "42.2.5"
  val h2 = "com.h2database" % "h2" % "1.4.200" % Test

  val sangriaVersion = "1.4.2"
  val sangria = "org.sangria-graphql" %% "sangria" % sangriaVersion
  val sangriaRelay = "org.sangria-graphql" %% "sangria-relay" % sangriaVersion
  val sangriaPlayJson = "org.sangria-graphql" %% "sangria-play-json" % "2.0.0"

  val scalapbJson4s = "com.thesamet.scalapb" %% "scalapb-json4s" % "0.7.2"

  val scalatest = "org.scalatest" %% "scalatest" % "3.2.9" % Test
  val scalacheck = "org.scalacheck" %% "scalacheck" % "1.14.3" % Test

  val jedisVersion = "3.3.0"
  val jedis = "redis.clients" % "jedis" % jedisVersion

  val slickVersion = "3.3.3"
  val slick = "com.typesafe.slick" %% "slick" % slickVersion
  val slickHikari = "com.typesafe.slick" %% "slick-hikaricp" % slickVersion
  val slickCodeGen = "com.typesafe.slick" %% "slick-codegen" % slickVersion

  val sttpVersion = "1.6.7"
  val sttpCore = "com.softwaremill.sttp" %% "core" % sttpVersion
  val sttpAkkaHttpBackend = "com.softwaremill.sttp" %% "akka-http-backend" % sttpVersion
  val sttpAsyncHttpClientBackendFuture = "com.softwaremill.sttp" %% "async-http-client-backend-future" % sttpVersion

  val tcnativeVersion = "2.0.20.Final"
  val tcnative = "io.netty" % "netty-tcnative" % tcnativeVersion
  val tcnativeLinux = "io.netty" % "netty-tcnative-boringssl-static" % tcnativeVersion classifier "linux-x86_64"
  val tcnativeOSX = "io.netty" % "netty-tcnative-boringssl-static" % tcnativeVersion classifier "osx-x86_64"
  val tcnativeWindows = "io.netty" % "netty-tcnative-boringssl-static" % tcnativeVersion classifier "windows-x86_64"

  val scopt = "com.github.scopt" %% "scopt" % "3.7.1"
  val scalaJava8Compat = "org.scala-lang.modules" %% "scala-java8-compat" % "0.9.1"

  val janino = "org.codehaus.janino" % "janino" % "3.0.12"
  val sentry = "io.sentry" % "sentry-logback" % "3.2.0"
  val typesafeConfig = "com.typesafe" % "config" % "1.3.3"

  val jacksonCbor = "com.fasterxml.jackson.dataformat" % "jackson-dataformat-cbor" % "2.9.8"

  val googleAPIClient = "com.google.api-client" % "google-api-client" % "1.28.0"


  
}
