name := "pricing"

version := "1.0.0"

scalaVersion := "2.11.7"

sbtVersion := "0.13.9"

mainClass := Some("com.carwale.pricing.Main")

libraryDependencies ++= Seq (
  "org.scalatest" % "scalatest_2.11" % "2.2.4" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "2.32.0" % "test"
)
