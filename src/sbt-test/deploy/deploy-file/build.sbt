version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.9.1"

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

seq(aetherSettings: _*)
