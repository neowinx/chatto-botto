name := "chatto-botto"

version := "1.0"

lazy val akkaVersion = "2.6.16"

lazy val commonSettings = Seq(
  scalaVersion := "2.13.6",

  libraryDependencies ++= Seq(
    "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
    "com.typesafe.akka" %% "akka-cluster-typed" % akkaVersion,
    "ch.qos.logback" % "logback-classic" % "1.2.3",
    "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
    "org.scalatest" %% "scalatest" % "3.1.0" % Test
  )
)

lazy val server = (project in file("server")).settings(commonSettings: _*).aggregate(chatroom).dependsOn(chatroom).enablePlugins(JavaServerAppPackaging)

lazy val client = (project in file("client")).settings(commonSettings: _*).aggregate(chatroom).dependsOn(chatroom).enablePlugins(JavaServerAppPackaging)

lazy val chatroom = (project in file("chatroom")).settings(commonSettings: _*)

