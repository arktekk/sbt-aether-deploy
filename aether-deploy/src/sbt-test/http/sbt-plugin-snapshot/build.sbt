ThisBuild / version := "0.1.0-SNAPSHOT"

name := "sbt-plugin"

organization := "com.example"

organizationName := "sbt-plugin"

scalaVersion := "2.12.12"

enablePlugins(SbtPlugin)

pomIncludeRepository := { _ => false }

publishTo := {
  if (isSnapshot.value) Some(("snapshots" at "http://localhost:19999/repository/maven-snapshots/").withAllowInsecureProtocol(true))
  else Some(("releases" at "http://localhost:19999/repository/maven-releases/").withAllowInsecureProtocol(true))
}

publishMavenStyle := true

overridePublishSettings

credentials += Credentials(new File("sonatype.credentials"))
