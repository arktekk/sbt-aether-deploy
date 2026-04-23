ThisBuild / credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials")

ThisBuild / pomIncludeRepository := { x =>
  false
}

ThisBuild / sbtPluginPublishLegacyMavenStyle := false

ThisBuild / publishTo := {
  val centralSnapshots = "https://central.sonatype.com/repository/maven-snapshots/"
  if (isSnapshot.value) Some("central-snapshots" at centralSnapshots)
  else localStaging.value
}

// Things we care about primarily because Maven Central demands them
ThisBuild / homepage := Some(url("http://github.com/arktekk/sbt-aether-deploy/"))

ThisBuild / startYear := Some(2012)

ThisBuild / licenses := Seq(("Apache 2", url("http://www.apache.org/licenses/LICENSE-2.0.txt")))

ThisBuild / scmInfo := Some(
  ScmInfo(
    url("http://github.com/arktekk/sbt-aether-deploy"),
    "scm:git:git://github.com/arktekk/sbt-aether-deploy.git",
    Some("scm:git:git@github.com:arktekk/sbt-aether-deploy.git")
  )
)

ThisBuild / developers += Developer(
  "hamnis",
  "Erlend Hamnaberg",
  "erlend@hamnaberg.net",
  url("http://twitter.com/hamnis")
)
