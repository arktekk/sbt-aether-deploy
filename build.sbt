ThisBuild / organization := "no.arktekk.sbt"

ThisBuild / description := "Deploy in SBT using Sonatype Aether"

ThisBuild / javacOptions := Seq("--release", "8")
ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked", "-release", "8")

ThisBuild / scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-Dplugin.version=" + (ThisBuild / version).value)
}

ThisBuild / scriptedBufferLog := false

lazy val aetherDeploy = (project in file("aether-deploy"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "aether-deploy",
    libraryDependencies ++= {
      val mavenResolverVersion = "1.9.23"
      Seq(
        "org.apache.maven.resolver" % "maven-resolver-supplier" % mavenResolverVersion
      )
    }
  )

lazy val aetherDeploySigned = (project in file("aether-deploy-signed"))
  .enablePlugins(SbtPlugin)
  .dependsOn(aetherDeploy)
  .settings(
    name := "aether-deploy-signed",
    addSbtPlugin("com.github.sbt" % "sbt-pgp" % "2.2.1")
  )

lazy val aetherDeployRoot = (project in file("."))
  .aggregate(aetherDeploy, aetherDeploySigned)
  .settings(publish / skip := true)
