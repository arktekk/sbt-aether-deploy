ThisBuild / packageOptions += {
  val title  = name.value
  val ver    = version.value
  val vendor = organization.value

  Package.ManifestAttributes(
    "Created-By"               -> "Simple Build Tool",
    "Built-By"                 -> System.getProperty("user.name"),
    "Build-Jdk"                -> System.getProperty("java.version"),
    "Specification-Title"      -> title,
    "Specification-Version"    -> ver,
    "Specification-Vendor"     -> vendor,
    "Implementation-Title"     -> title,
    "Implementation-Version"   -> ver,
    "Implementation-Vendor-Id" -> vendor,
    "Implementation-Vendor"    -> vendor
  )
}

ThisBuild / credentials := List (
  Credentials(
    "",
    "oss.sonatype.org",
    System.getenv("PUBLISH_USER"),
    System.getenv("PUBLISH_USER_PASSPHRASE")
  )
)

ThisBuild / pomIncludeRepository := { x =>
  false
}

ThisBuild / publishTo := {
  if (version.value.trim().endsWith("SNAPSHOT")) {
    Some(Opts.resolver.sonatypeSnapshots)
  } else {
    Some(Opts.resolver.sonatypeStaging)
  }
}

// Things we care about primarily because Maven Central demands them
ThisBuild / homepage := Some(new URL("http://github.com/arktekk/sbt-aether-deploy/"))

ThisBuild / startYear := Some(2012)

ThisBuild / licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")))

ThisBuild / scmInfo := Some(
  ScmInfo(
    new URL("http://github.com/arktekk/sbt-aether-deploy"),
    "scm:git:git://github.com/arktekk/sbt-aether-deploy.git",
    Some("scm:git:git@github.com:arktekk/sbt-aether-deploy.git")
  )
)

ThisBuild / developers += Developer(
  "hamnis",
  "Erlend Hamnaberg",
  "erlend@hamnaberg.net",
  new URL("http://twitter.com/hamnis")
)
