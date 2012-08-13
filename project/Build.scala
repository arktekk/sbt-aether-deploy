import sbt._
import Keys._

object Build extends sbt.Build {
  import Dependencies._
	
  lazy val buildSettings = Defaults.defaultSettings ++ Seq(
	organization := "no.arktekk.sbt"
  )
  lazy val root = Project(
    id = "sbt-aether-deploy",
    base = file("."),
    settings = buildSettings ++ Seq(
      description := "Deploy in SBT using Sonatype Aether",
      name := "aether-deploy", 
      libraryDependencies := deps,
      scalacOptions := Seq("-deprecation", "-unchecked"),
      manifestSetting,
      publishSetting,
      sbtPlugin := true,
      credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials"),
	  pomIncludeRepository := { x => false }

    ) ++ mavenCentralFrouFrou ++ ScriptedPlugin.scriptedSettings
  )

  object Dependencies {
	val wagonVersion = "2.2"
	val mavenVersion = "3.0.4"
	
	val deps = Seq(
	  "org.apache.maven" % "maven-aether-provider" % mavenVersion,
	  "org.sonatype.aether" % "aether-connector-wagon" % "1.13.1",
	  "org.sonatype.aether" % "aether-connector-file" % "1.13.1",
    "org.sonatype.aether" % "aether-connector-asynchttpclient" % "1.13.1"
	  /*"org.apache.maven.wagon" % "wagon-provider-api" % wagonVersion,
	  "org.sonatype.maven" % "wagon-ahc" % "1.2.1",
	  "org.codehaus.plexus" % "plexus-utils" % "2.1",
	  "org.apache.maven.wagon" % "wagon-ftp" % wagonVersion,
	  "org.apache.maven.wagon" % "wagon-ssh" % wagonVersion*/
    )
  }

  object Resolvers {
    val sonatypeNexusSnapshots = "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
    val sonatypeNexusStaging = "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
  }

  lazy val manifestSetting = packageOptions <+= (name, version, organization) map {
    (title, version, vendor) =>
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

  lazy val publishSetting = publishTo <<= (version) { version: String =>
    if (version.trim.endsWith("SNAPSHOT"))
      Some(Resolvers.sonatypeNexusSnapshots)
    else
      Some(Resolvers.sonatypeNexusStaging)
  }

  // Things we care about primarily because Maven Central demands them
  lazy val mavenCentralFrouFrou = Seq(
    homepage := Some(new URL("http://github.com/arktekk/sbt-aether-deploy/")),
    startYear := Some(2012),
    licenses := Seq(("Apache 2", new URL("http://www.apache.org/licenses/LICENSE-2.0.txt"))),
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
  )	
	
}
