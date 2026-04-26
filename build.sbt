organization := "no.arktekk.sbt"
description  := "Deploy in SBT using Sonatype Aether"

val platforms: Seq[(scala: String, sbt: String)] = Seq(
  (scala = "2.12.21", sbt = "1.11.0"),
  (scala = "3.8.3", sbt = "2.0.0-RC12")
)

scalaVersion := platforms.head.scala

lazy val aetherDeploy =
  projectMatrix
    .in(file("aether-deploy"))
    .jvmPlatform(scalaVersions = platforms.map(_.scala))
    .enablePlugins(SbtPlugin)
    .settings(commonSettings)
    .settings(scriptedSettings)
    .settings(
      name := "aether-deploy",
      addSbtPlugin("com.github.sbt" % "sbt2-compat" % "0.1.0"),
      libraryDependencies ++= {
        Seq(
          // Exclude plexus-utils transitively so resolution on cannot silently upgrade to plexus-utils 4.x
          // which dropped the `org.codehaus.plexus.util.xml.pull` package we depend on at runtime.
          // See arktekk/sbt-aether-deploy#43.
          ("org.apache.maven.resolver" % "maven-resolver-supplier" % "1.9.23")
            .exclude("org.codehaus.plexus", "plexus-utils"),
          "org.codehaus.plexus"        % "plexus-utils"            % "3.6.0",
          "org.scala-lang.modules"    %% "scala-collection-compat" % "2.14.0"
        )
      }
    )

lazy val aetherDeploySigned =
  projectMatrix
    .in(file("aether-deploy-signed"))
    .jvmPlatform(scalaVersions = platforms.map(_.scala))
    .enablePlugins(SbtPlugin)
    .dependsOn(aetherDeploy)
    .settings(commonSettings)
    .settings(scriptedSettings)
    .settings(
      name := "aether-deploy-signed",
      addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
    )

lazy val aetherDeployRoot =
  projectMatrix
    .in(file("."))
    .jvmPlatform(scalaVersions = platforms.map(_.scala))
    .aggregate(aetherDeploy)
    .aggregate(aetherDeploySigned)
    .settings(publish / skip := true)

def commonSettings =
  val scala3 = Def.setting(scalaBinaryVersion.value == "3")
  Seq(
    pluginCrossBuild / sbtVersion := platforms.find(_.scala == scalaVersion.value).get.sbt,
    javacOptions                  := {
      if (scala3.value) Seq("--release", "17") else Seq("--release", "8")
    },
    scalacOptions                 := {
      val shared = Seq("-deprecation", "-unchecked")
      if (scala3.value) shared :+ "-release:17" else shared :+ "-release:8"
    }
  )

def scriptedSettings = Seq(
  scriptedLaunchOpts := {
    scriptedLaunchOpts.value ++
      Seq("-Xmx1024M", "-Dplugin.version=" + version.value)
  },
  scriptedBufferLog  := false
)
