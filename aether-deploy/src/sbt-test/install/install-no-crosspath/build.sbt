import aether.AetherKeys._

ThisBuild / version  := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.10.2"

overridePublishLocalSettings

crossPaths := false

aetherLocalRepo := file(".") / "target" / "repo"
