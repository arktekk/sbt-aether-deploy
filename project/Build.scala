import sbt._
import Keys._

object Build extends sbt.Build {
  import Dependencies._
	
  lazy val root = Project(
    id = "sbt-aether-deploy",
    base = file("."),
    settings =  Defaults.defaultSettings ++ Seq(
      organization := "no.arktekk.sbt",
      description := "Deploy in SBT using Sonatype Aether",
      name := "aether-deploy", 
      libraryDependencies ++= deps,
      scalacOptions := Seq("-deprecation", "-unchecked"),
      manifestSetting,
      publishSetting,
      sbtPlugin := true,
      credentials += Credentials(Path.userHome / ".sbt" / "arktekk-credentials"),
	    pomIncludeRepository := { x => false },
      CrossBuilding.crossSbtVersions := Seq("0.13")
    ) ++ mavenCentralFrouFrou ++ ScriptedPlugin.scriptedSettings
  ).settings(net.virtualvoid.sbt.cross.CrossPlugin.crossBuildingSettings: _*)

  object Dependencies {
    val mavenVersion = "3.2.1"
    val aetherVersion = "0.9.0.v20140226"

    val deps = Seq(
        "org.apache.maven" % "maven-aether-provider" % mavenVersion intransitive(),
        "org.apache.maven" % "maven-model-builder" % mavenVersion intransitive(),
        "org.apache.maven" % "maven-repository-metadata" % mavenVersion intransitive(),
        "org.codehaus.plexus" % "plexus-interpolation" % "1.19" intransitive(),
        "org.apache.maven" % "maven-model" % mavenVersion,
        "org.glassfish.hk2.external" % "javax.inject" % "2.2.0-b14" % "provided",
        "org.codehaus.plexus" % "plexus-component-annotations" % "1.5.5" % "provided",
        "org.eclipse.aether" % "aether-impl" % aetherVersion,
        "org.eclipse.aether" % "aether-connector-basic" % aetherVersion,
        "org.eclipse.aether" % "aether-transport-http" % aetherVersion,
        "org.eclipse.aether" % "aether-transport-file" % aetherVersion,
        "org.eclipse.aether" % "aether-transport-wagon" % aetherVersion,
        "ch.qos.logback" % "logback-classic" % "1.0.13"
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
