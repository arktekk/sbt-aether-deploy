version := "0.1"

name := "deploy-file"

organization := "deploy-file"

scalaVersion := "2.9.1"

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

seq(aetherSettings: _*)

aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, com.typesafe.sbt.pgp.PgpKeys.signedArtifacts in Compile) map {
  (coords: aether.MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
    aether.Aether.createArtifact(artifacts, pom, coords, mainArtifact)
}

useGpg := true

//gpgCommand := "gpg2"

