version in ThisBuild := "0.1.0-SNAPSHOT"

name := "two-ten"

organization := "two-ten"

scalaVersion := "2.10.0"

publishTo := Some("Nexus" at "http://localhost:19999/repository/maven-snapshots/")

overridePublishSettings

credentials += Credentials(new File("sonatype.credentials"))
