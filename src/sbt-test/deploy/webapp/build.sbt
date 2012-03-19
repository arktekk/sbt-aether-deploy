version := "0.1"

name := "webapp"

organization := "deploy"

aether.AetherKeys.deployRepository  := "foo" at (file(".") / "target" / "repo").toURI.toURL.toString

seq(webappSettings :_*)

seq(aether.Aether.withPackage(packageWar) :_*)
