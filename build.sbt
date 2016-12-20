name := "mfdat"
version := "0.1"
scalaVersion := "2.12.1"

scalacOptions ++= Seq(
  "-deprecation",
  "-unchecked",
  "-feature",
  "-Xfatal-warnings"
)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-actor" % "2.4.14",
  "com.typesafe.akka" %% "akka-http" % "10.0.0",
  "ch.megard" %% "akka-http-cors" % "0.1.10",
  "org.postgresql" % "postgresql" % "9.4.1212"
)
