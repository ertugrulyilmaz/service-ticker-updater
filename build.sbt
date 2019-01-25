
name := "bundle-service-ticker-updater"

version := "1.0"

scalaVersion := "2.12.2"

scalacOptions := Seq(
  "-deprecation",
  "-encoding", "UTF-8",
  "-feature",
  "-language:existentials",
  "-language:higherKinds",
  "-language:implicitConversions",
  "-unchecked",
  "-Xfatal-warnings",
  "-Xlint",
  "-Yno-adapted-args",
  "-Ywarn-dead-code",
  "-Ywarn-numeric-widen",
  "-Ywarn-value-discard",
  "-Xfuture",
  "-Ywarn-unused-import"
)

libraryDependencies ++= {

  val akkaV = "2.4.20"
  val logbackV = "1.2.3"
  val ningV = "1.9.40"
  val scalaLoggingV = "3.7.2"
  val slf4jV = "1.7.7"
  val json4sV = "3.5.3"
  val slickV = "3.2.0"
  val hikariV = "2.6.2"
  val mysqlV = "6.0.6"
  val jsoupV = "1.8.3"
  val dynamodbV = "1.11.179"
  val redisV = "1.8.0"

  Seq(
    "com.amazonaws" % "aws-java-sdk-dynamodb" % dynamodbV,

    "com.typesafe.akka" %% "akka-actor" % akkaV,
    "com.typesafe.akka" %% "akka-stream" % akkaV,
    "com.ning" % "async-http-client" % ningV,

    "org.json4s" %% "json4s-jackson" % json4sV,

    "com.github.etaty" %% "rediscala" % redisV,

    "com.typesafe.slick" %% "slick" % slickV,
    "mysql" % "mysql-connector-java" % mysqlV,
    "com.zaxxer" % "HikariCP" % hikariV,
    "org.jsoup" % "jsoup" % jsoupV,

    "ch.qos.logback" % "logback-classic" % logbackV,
    "com.typesafe.akka" %% "akka-slf4j" % akkaV,
    "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingV,
    "org.slf4j" % "slf4j-api" % slf4jV
  )

}

//mainClass in assembly := some("network.bundle.ticker.Main")
assemblyJarName := "bundle-service-ticker-updater.jar"

val meta = """META.INF(.)*""".r
assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", xs @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".html" => MergeStrategy.first
  case n if n.startsWith("reference.conf") => MergeStrategy.concat
  case n if n.endsWith(".conf") => MergeStrategy.concat
  case meta(_) => MergeStrategy.discard
  case x => MergeStrategy.first
}
