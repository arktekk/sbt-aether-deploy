version in ThisBuild  := "0.1"

name := "webapp"

organization := "deploy"

scalaVersion := "2.11.6"

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

enablePlugins(WarPlugin)

overridePublishSettings
