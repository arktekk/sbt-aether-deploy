version := "0.1"

name := "webapp"

organization := "deploy"

scalaVersion := "2.9.1"

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

seq(webappSettings :_*)

seq(aetherSettings: _*)
