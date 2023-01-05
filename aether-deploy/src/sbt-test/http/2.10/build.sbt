ThisBuild / version := "0.1.0"

name := "two-ten"

organization := "com.example"

organizationName := "two-ten"

scalaVersion := "2.10.0"

publishTo := {
  if (isSnapshot.value) Some(("snapshots" at "http://localhost:19999/repository/maven-snapshots/").withAllowInsecureProtocol(true))
  else Some(("releases" at "http://localhost:19999/repository/maven-releases/").withAllowInsecureProtocol(true))
}

overridePublishSettings

credentials += Credentials(new File("sonatype.credentials"))
