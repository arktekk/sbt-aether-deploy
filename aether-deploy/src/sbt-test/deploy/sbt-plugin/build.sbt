ThisBuild / version  := "0.1"

name := "sbt-plugin"

organization := "sbt-plugin"

enablePlugins(SbtPlugin)

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings
