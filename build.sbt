name := "pricing"

version := "1.0.0"

scalaVersion := "2.11.7"

sbtVersion := "0.13.9"

mainClass := Some("Main")

libraryDependencies ++= Seq (
  "com.typesafe.play" %% "play-ws" % "2.4.0",
  "com.typesafe.play" %% "play-json" % "2.4.0",
  "org.jsoup" % "jsoup" % "1.8.3",
  "org.scalatest" %% "scalatest" % "2.2.4" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.32.0" % "test"
)
