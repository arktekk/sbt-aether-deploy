ThisBuild / version := "0.1"

name := "deploy-sbt-sonatype"

organization := "deploy-sbt-sonatype"

scalaVersion := "3.3.1"

publishTo := sonatypePublishToBundle.value
