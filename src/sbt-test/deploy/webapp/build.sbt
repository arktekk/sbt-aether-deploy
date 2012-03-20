version := "0.1"

name := "webapp"

organization := "deploy"

deployRepository  := "foo" at (file(".") / "target" / "repo").toURI.toURL.toString

seq(webappSettings :_*)

seq(aetherSettings: _*)