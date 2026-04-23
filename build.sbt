ThisBuild / organization := "no.arktekk.sbt"

ThisBuild / description := "Deploy in SBT using Sonatype Aether"

ThisBuild / scalaVersion := (ThisBuild / crossScalaVersions).value.head
ThisBuild / crossScalaVersions := Seq("2.12.21", "3.8.3")
val isScala3 = Def.setting(scalaBinaryVersion.value == "3")

ThisBuild / scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + (ThisBuild / version).value)
}

ThisBuild / scriptedBufferLog := false

val commonSettings = Seq(
  pluginCrossBuild / sbtVersion :=  { if (isScala3.value) "2.0.0-RC12" else "1.11.0" },
  javacOptions := { if (isScala3.value) Seq("--release", "17") else Seq("--release", "8") },
  scalacOptions := {
    val shared = Seq("-deprecation", "-unchecked")
    if (isScala3.value) shared :+ "-release:17" else shared :+ "-release:8"
  }
)

lazy val aetherDeploy = (project in file("aether-deploy"))
  .enablePlugins(SbtPlugin)
  .settings(commonSettings)
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
    },
    scriptedDependencies := {
      scriptedDependencies.value: Unit
      val scala3             = isScala3.value
      val sbt2Skipped = Seq(
        "deploy/deploy-sbt-sonatype", // Sonatype publishing natively supported in sbt 2.x
        "deploy/webapp"               // xsbt-web-plugin not yet released for sbt 2.x
      )
      sbt2Skipped.foreach { rel =>
        val marker = sbtTestDirectory.value / rel / "disabled"
        if (scala3) IO.touch(marker) else IO.delete(marker)
      }
    }
  )

lazy val aetherDeploySigned = (project in file("aether-deploy-signed"))
  .enablePlugins(SbtPlugin)
  .dependsOn(aetherDeploy)
  .settings(commonSettings)
  .settings(
    name := "aether-deploy-signed",
    addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.3.1")
  )

lazy val aetherDeployRoot = (project in file("."))
  .aggregate(aetherDeploy, aetherDeploySigned)
  .settings(publish / skip := true)
