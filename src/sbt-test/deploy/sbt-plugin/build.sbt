version in ThisBuild  := "0.1"

name := "sbt-plugin"

organization := "sbt-plugin"

enablePlugins(SbtPlugin)
crossSbtVersions := Seq("0.13.17")

publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings
