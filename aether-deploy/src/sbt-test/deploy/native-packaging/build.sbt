import aether.AetherKeys._

ThisBuild / version := "0.1"

name := "packaged-app"

organization := "deploy"

scalaVersion := "2.12.21"

enablePlugins(JavaAppPackaging)

publishTo := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings

// Setting-level helper mirroring sbt-native-packager's `SettingsHelper.addPackage`:
// the task's File/HashedVirtualFileRef return type and the sbt 2.x task-value cache
// are handled internally, so the consuming build file needs no `fileConverter.value`,
// no `implicit val`, and no `Def.uncached` wrapping.
attachSubArtifact(Universal / packageBin, "dist", "zip")
