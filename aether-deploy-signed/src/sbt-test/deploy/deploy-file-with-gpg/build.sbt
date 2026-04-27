ThisBuild / version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "3.8.3"

publishTo := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSignedSettings

crossPaths := false