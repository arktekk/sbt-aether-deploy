ThisBuild / version  := "0.1"

name := "multi-publish"

organization := "multi-publish"

scalaVersion := "2.10.0"

crossScalaVersions := Seq("2.10.0", "2.9.2", "2.9.1")

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings
