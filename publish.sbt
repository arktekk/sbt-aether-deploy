
packageOptions <+= (name, version, organization) map { (title, version, vendor) =>
    Package.ManifestAttributes(
      "Created-By" -> "Simple Build Tool",
      "Built-By" -> System.getProperty("user.name"),
      "Build-Jdk" -> System.getProperty("java.version"),
      "Specification-Title" -> title,
      "Specification-Version" -> version,
      "Specification-Vendor" -> vendor,
      "Implementation-Title" -> title,
      "Implementation-Version" -> version,
      "Implementation-Vendor-Id" -> vendor,
      "Implementation-Vendor" -> vendor
    )
}

credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials")

pomIncludeRepository := { x => false }

publishTo <<= (version) apply {(v: String) =>
  if (v.trim().endsWith("SNAPSHOT")) {
    Some(Opts.resolver.sonatypeSnapshots) }
  else {
    Some(Opts.resolver.sonatypeStaging)
  }
}

// Things we care about primarily because Maven Central demands them
homepage := Some(new URL("http://github.com/arktekk/sbt-aether-deploy/"))

startYear := Some(2012)

licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt")))

pomExtra <<= (pomExtra, name, description) {(pom, name, desc) => pom ++ xml.Group(
  <scm>
    <url>http://github.com/arktekk/sbt-aether-deploy</url>
    <connection>scm:git:git://github.com/arktekk/sbt-aether-deploy.git</connection>
    <developerConnection>scm:git:git@github.com:arktekk/sbt-aether-deploy.git</developerConnection>
  </scm>
  <developers>
    <developer>
      <id>hamnis</id>
      <name>Erlend Hamnaberg</name>
      <url>http://twitter.com/hamnis</url>
    </developer>
  </developers>
)}
