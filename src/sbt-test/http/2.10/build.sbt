version in ThisBuild := "0.1"

name := "two-ten"

organization := "two-ten"

scalaVersion := "2.10.0"

publishTo  := Some("Nexus" at "http://localhost:8081/repository/maven-releases/")

overridePublishSettings

credentials += Credentials("Sonatype Nexus Repository Manager", "localhost", "admin", "admin123")
