ThisBuild / version := "0.1"

name := "sbt-plugin"

organization := "sbt-plugin"

enablePlugins(SbtPlugin)

publishTo := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

overridePublishSettings

// validates the published sbt-plugin artefact exists under the coordinate directory appropriate
// for the current sbt binary version.
//  - sbt 1.x : <name>_<scalaBinaryVersion>_<sbtBinaryVersion>  e.g. sbt-plugin_2.12_1.0
//  - sbt 2.x : <name>_sbt<sbtBinaryVersion>_<scalaBinaryVersion>  e.g. sbt-plugin_sbt2_3
TaskKey[Unit]("checkPublished") := {
  val sbtBin = (pluginCrossBuild / sbtBinaryVersion).value
  val scalaBin = scalaBinaryVersion.value
  val majorOpt = sbtBin.split('.').headOption.flatMap(s => scala.util.Try(s.toInt).toOption)
  val suffix = majorOpt match {
    case Some(m) if m >= 2 => s"_sbt${m}_$scalaBin"
    case _                 => s"_${scalaBin}_$sbtBin"
  }
  val repo = (baseDirectory.value / "target" / "repo" / "sbt-plugin" / s"sbt-plugin$suffix")
  val ver = repo / "0.1"
  val expected = List(
    repo / "maven-metadata.xml",
    ver,
    ver / s"sbt-plugin$suffix-0.1.jar",
    ver / s"sbt-plugin$suffix-0.1.pom",
    ver / s"sbt-plugin$suffix-0.1-sources.jar",
    ver / s"sbt-plugin$suffix-0.1-javadoc.jar"
  )
  expected.foreach { f =>
    if (!f.exists()) sys.error(s"Expected published artefact not found: ${f.getAbsolutePath}")
  }
  val forbidden = List(
    ver / "sbt-plugin-0.1.jar",
    ver / "sbt-plugin-0.1.pom",
    ver / "sbt-plugin-0.1-sources.jar",
    ver / "sbt-plugin-0.1-javadoc.jar"
  )
  forbidden.foreach { f =>
    if (f.exists()) sys.error(s"Unexpected legacy-style artefact present: ${f.getAbsolutePath}")
  }
}
