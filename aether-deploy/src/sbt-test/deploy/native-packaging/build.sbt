import aether.AetherKeys._

version in ThisBuild  := "0.1"

name := "packaged-app"

organization := "deploy"

scalaVersion := "2.12.2"

enablePlugins(JavaAppPackaging)

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings

aetherArtifact := {
    val artifact = aetherArtifact.value
    artifact.attach((packageBin in Universal).value, "dist", "zip")
}
