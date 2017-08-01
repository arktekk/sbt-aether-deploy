
packageOptions += {  
  val title = name.value
  val ver = version.value
  val vendor = organization.value

  Package.ManifestAttributes(
    "Created-By" -> "Simple Build Tool",
    "Built-By" -> System.getProperty("user.name"),
    "Build-Jdk" -> System.getProperty("java.version"),
    "Specification-Title" -> title,
    "Specification-Version" -> ver,
    "Specification-Vendor" -> vendor,
    "Implementation-Title" -> title,
    "Implementation-Version" -> ver,
    "Implementation-Vendor-Id" -> vendor,
    "Implementation-Vendor" -> vendor
  )
}

credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials")

pomIncludeRepository := { x => false }

publishTo := {
  if (version.value.trim().endsWith("SNAPSHOT")) {
    Some(Opts.resolver.sonatypeSnapshots) }
  else {
    Some(Opts.resolver.sonatypeStaging)
  }
}

// Things we care about primarily because Maven Central demands them
homepage := Some(new URL("http://github.com/arktekk/sbt-aether-deploy/"))

startYear := Some(2012)

licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")))

scmInfo := Some(ScmInfo(
  new URL("http://github.com/arktekk/sbt-aether-deploy"), 
  "scm:git:git://github.com/arktekk/sbt-aether-deploy.git", 
  Some("scm:git:git@github.com:arktekk/sbt-aether-deploy.git"
  )
))

developers += Developer(
  "hamnis",
  "Erlend Hamnaberg",
  "erlend@hamnaberg.net",
  new URL("http://twitter.com/hamnis")
)
