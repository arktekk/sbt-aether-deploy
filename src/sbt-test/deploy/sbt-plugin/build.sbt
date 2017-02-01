version in ThisBuild  := "0.1"

name := "sbt-plugin"

organization := "sbt-plugin"

sbtPlugin := true

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings
