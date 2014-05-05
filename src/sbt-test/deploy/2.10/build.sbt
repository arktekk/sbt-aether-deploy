version := "0.1"

name := "two-ten"

organization := "two-ten"

scalaVersion := "2.10.0"

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

aetherPublishSettings
