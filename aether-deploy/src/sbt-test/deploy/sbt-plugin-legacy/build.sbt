ThisBuild / version := "0.1"

name := "sbt-plugin-legacy"

organization := "legacy"

enablePlugins(SbtPlugin)

publishTo := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings

aether.AetherKeys.aetherLegacyPluginStyle := true
