version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.9.1"

aetherPublishBothSettings

aetherLocalRepo := file(".") / "target" / "repo"
