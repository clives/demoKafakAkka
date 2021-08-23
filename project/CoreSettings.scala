import sbt.Keys.{resolvers, _}
import sbt._

/**
  * Enabled automatically across all projects,
  * the `CoreSettings` plugin sets up java and scala compiler options,
  * includes a core set of ubiquitous dependencies,
  * and auto-imports variables for use in build.sbt files.
  */
object CoreSettings extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def buildSettings = Seq(
    organization := "demo",
    scalaVersion := "2.12.12",
    javacOptions ++= javaCompiler,
    scalacOptions ++= scalaCompiler(scalaVersion.value),
    libraryDependencies ++= ubiquitousDependencies    
  )


  private val InitialBasePort = 20000
  private val BasePortStep = 100
  private val basePort = Iterator.from(InitialBasePort, BasePortStep)
  private def nextBasePort: Int = synchronized(basePort.next)

  override def projectSettings: Seq[Setting[_]] = Seq(
    /*
    D - show all durations
    F - show full stack traces
    N - drop TestStarting events
    C - drop TestSucceeded events
    X - drop TestIgnored events
    E - drop TestPending events
    O - drop InfoProvided events
    P - drop ScopeOpened events
    Q - drop ScopeClosed events
    R - drop ScopePending events
    M - drop MarkupProvided events
     */
    testOptions in Test ++= Seq(
      Tests.Argument("-oDFNCXEOPQRM"),
      Tests.Argument(s"-Dtest.base.port=$nextBasePort")
    ),
    Keys.logBuffered in Test := false, // recommended, see: http://www.scalatest.org/user_guide/using_scalatest_with_sbt
    parallelExecution in Test := false,
    javaOptions in Test ++= Seq("-Dkamon.disable=true"),
    // console can't handle fatal warnings
    scalacOptions in (Compile, console) --= Seq("-Ywarn-unused:imports", "-Xfatal-warnings"),
    // Use cached resolution
    updateOptions := updateOptions.value.withCachedResolution(true),
    resourceGenerators in Compile += Def.task {
      val file = (resourceManaged in Compile).value / "sentry.properties"
      val contents = s"""release=${version.value}""".stripMargin
      IO.write(file, contents)
      Seq(file)
    }.taskValue,
    Compile / doc / sources := Seq.empty,
    Compile / packageDoc / publishArtifact := false
  )

  def javaCompiler = sys.props("java.specification.version") match {
    case "11" =>
      // https://stackoverflow.com/questions/43102787/what-is-the-release-flag-in-the-java-9-compiler
      Seq("--release", "8")
    case _ =>
      Seq(
        "-source",
        "1.8",
        "-target",
        "1.8"
      )
  }

  def scalaCompiler(scalaVersion: String): Seq[String] = {
    Seq(
      "-target:jvm-1.8",
      "-deprecation",
      "-feature",
      "-Xfuture",
      "-Yno-adapted-args",
      "-encoding",
      "utf8",
      "-language:implicitConversions",
      "-language:reflectiveCalls",
      "-Xmax-classfile-name",
      "140"
    ) ++ (CrossVersion.partialVersion(scalaVersion) match {
      case Some((2, n)) if n == 12 =>
        Seq(
          "-Xsource:2.12",
          "-Yrangepos",
          "-Ybackend-parallelism",
          "8", // https://github.com/scala/scala/pull/6124
      //C.S to not commit the comment    "-Ywarn-unused:imports", // Warn if an import selector is not referenced.
          "-unchecked",
          // Use a subset of xlint.
          "-Xlint:adapted-args", // Warn if an argument list is modified to match the receiver.
          "-Xlint:by-name-right-associative", // By-name parameter of right associative operator.
          "-Xlint:constant", // Evaluation of a constant arithmetic expression results in an error.
          "-Xlint:delayedinit-select", // Selecting member of DelayedInit.
          "-Xlint:doc-detached", // A Scaladoc comment appears to be detached from its element.
          "-Xlint:inaccessible", // Warn about inaccessible types in method signatures.
          "-Xlint:infer-any", // Warn when a type argument is inferred to be `Any`.
          "-Xlint:missing-interpolator", // A string literal appears to be missing an interpolator id.
          "-Xlint:nullary-override", // Warn when non-nullary `def f()' overrides nullary `def f'.
          "-Xlint:nullary-unit", // Warn when nullary methods return Unit.
          "-Xlint:option-implicit", // Option.apply used implicit view.
          "-Xlint:package-object-classes", // Class or object defined in package object.
          "-Xlint:poly-implicit-overload", // Parameterized overloaded implicit methods are not visible as view bounds.
          "-Xlint:private-shadow", // A private field (or class parameter) shadows a superclass field.
          "-Xlint:stars-align", // Pattern sequence wildcard must align with sequence component.
          "-Xlint:type-parameter-shadow", // A local type parameter shadows a type already in scope.
          "-Xlint:unsound-match",
          "-Xfatal-warnings"
        )
    })
  }

  import Dependencies._

  val ubiquitousDependencies = Seq()

}
