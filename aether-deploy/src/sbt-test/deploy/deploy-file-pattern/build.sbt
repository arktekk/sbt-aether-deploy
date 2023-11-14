ThisBuild / version := "0.1"

name := "deploy-file-pattern"

organization := "deploy-file-pattern"

scalaVersion := "3.3.1"

publishTo := Some(Resolver.file("foo", file(".") / "target" / "repo"))
