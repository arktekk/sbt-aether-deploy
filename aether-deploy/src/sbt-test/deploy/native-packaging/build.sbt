import aether.AetherKeys._

ThisBuild / version  := "0.1"

name := "packaged-app"

organization := "deploy"

scalaVersion := "2.12.2"

enablePlugins(JavaAppPackaging)

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings

aetherArtifact := {
    val artifact = aetherArtifact.value
    artifact.attach((Universal / packageBin).value, "dist", "zip")
}
