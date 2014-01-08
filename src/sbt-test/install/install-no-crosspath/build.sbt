version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.10.2"

seq(aetherPublishBothSettings: _*)

crossPaths := false

aetherLocalRepo := file(".") / "target" / "repo"