import java.time.{Duration, LocalDateTime}

import Dependencies._

name := "demo"

val silencerVersion = "1.7.0"




// Default settings for all projects
inThisBuild(
  Seq(
    version := "2021.08.02",
    resolvers+="cakesolutions" at "https://dl.bintray.com/cakesolutions/maven/",
    excludeDependencies += "commons-logging" % "commons-logging",
    excludeDependencies += "org.slf4j" % "slf4j-log4j12",
    excludeDependencies += "log4j" % "log4j",
    // https://github.com/sbt/sbt/issues/3537
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageDoc) := false,
    // https://sbt-native-packager.readthedocs.io/en/stable/formats/universal.html#skip-packagedoc-task-on-stage
    mappings in (Compile, packageDoc) := Seq.empty,
    scalacOptions ++= Seq(
      "-P:silencer:pathFilters=target/.*",
      s"-P:silencer:sourceRoots=${baseDirectory.value.getCanonicalPath}"
    ),
    libraryDependencies ++= Seq(
      compilerPlugin("com.github.ghik" % "silencer-plugin" % silencerVersion cross CrossVersion.full),
      "com.github.ghik" % "silencer-lib" % silencerVersion % Provided cross CrossVersion.full
    ),
    crossScalaVersions := Seq(scalaVersion.value, "2.12.12"),
    scalacOptions ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, n)) if n >= 12 && sys.props("java.specification.version") > "9" =>
        Seq("-release", "8")
      case _ =>
        Nil
    })
  )
)

/**
  * Settings added here should be appropriate for all projects in the modules directory.
  *
  * NOTE: Settings applied to all sbt projects are maintained in `project/CoreSettings.scala`.
  */
def modulesProject(name: String): Project = {
  Project(name, file(s"modules/$name"))
    .settings(
      javaOptions in Test ++= Seq(
        "-Dconfig.resource=test.conf",
        // https://www.scala-sbt.org/1.x/docs/Paths.html#Constructing+a+File
        s"-Ddev.root=${(ThisBuild / baseDirectory).value}/dev",
        "-Dlogger.file=../logging/src/test/resources/logback/test.xml",
        "-Dlogback.configurationFile=../logging/src/test/resources/logback/test.xml"
      )
    )
}



// Tweak the C2 compiler to inline more code based on Scala's usage patterns.
// These settings are now the defaults in sbt's jvmopts, so they're pretty safe.
// https://twitter.com/leifwickland/status/1179419045055086595
val inlineOptions = """
  |addJava "-XX:MaxInlineLevel=18"
  |addJava "-XX:MaxInlineSize=270"
  |addJava "-XX:MaxTrivialSize=12"
  |""".stripMargin

// https://www.slideshare.net/PoonamBajaj5/lets-learn-to-talk-to-gc-logs-in-java-9
// https://dzone.com/articles/disruptive-changes-to-gc-logging-in-java-9
// https://openjdk.java.net/jeps/158
val gcMonitoring =
  """
  |addJava "-Xlog:gc*=debug:file=/var/log/gc.log.started-at-%t.log:utctime,level,tags,tid:filesize=0"
  |addJava "-XX:ErrorFile=/var/log/fatal.log"
  |addJava "-XX:+PerfDisableSharedMem"
  |""".stripMargin

val logbackXML = """
 |addJava "-Dlogback.configurationFile=${app_home}/../conf/logback.xml"
 |""".stripMargin

val universalJvmOptions = gcMonitoring + inlineOptions



lazy val eventProducer = modulesProject("eventProducer")
 .dependsOn(base % "compile->compile;test->test")

lazy val eventConsumer = modulesProject("eventConsumer")
 .dependsOn(base % "compile->compile;test->test")

lazy val base = modulesProject("base")
  /*.dependsOn(
    core % "compile->compile;test->test",
    logging,
    kamon % "compile->compile;test->test",
    telemetry
  )*/