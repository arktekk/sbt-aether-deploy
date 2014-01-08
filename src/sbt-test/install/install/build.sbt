version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.9.1"

seq(aetherPublishBothSettings: _*)

aetherLocalRepo := file(".") / "target" / "repo"