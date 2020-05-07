ThisBuild / organization := "no.arktekk.sbt"

ThisBuild / description := "Deploy in SBT using Sonatype Aether"

ThisBuild / scalacOptions := Seq("-deprecation", "-unchecked")

ThisBuild / scriptedLaunchOpts := {
  scriptedLaunchOpts.value ++
    Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + (version in ThisBuild).value)
}

ThisBuild / scriptedBufferLog := false

lazy val aetherDeploy = (project in file("aether-deploy"))
  .enablePlugins(SbtPlugin)
  .settings(
    name := "aether-deploy",
    libraryDependencies ++= {
      val mavenVersion = "3.6.3"
      val mavenResolverVersion = "1.4.1"
      Seq(
        "org.apache.maven" % "maven-resolver-provider" % mavenVersion,
        "org.apache.maven.resolver" % "maven-resolver-api" % mavenResolverVersion,
        "org.apache.maven.resolver" % "maven-resolver-impl" % mavenResolverVersion,
        "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
        "org.apache.maven.resolver" % "maven-resolver-connector-basic" % mavenResolverVersion,
        "org.apache.maven.resolver" % "maven-resolver-transport-http" % mavenResolverVersion,
        "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
        "commons-logging" % "commons-logging" % "1.2"
      )
    }
  )

lazy val aetherDeploySigned = (project in file("aether-deploy-signed"))
  .enablePlugins(SbtPlugin)
  .dependsOn(aetherDeploy)
  .settings(
    name := "aether-deploy-signed",
    addSbtPlugin("com.jsuereth" % "sbt-pgp" % "2.0.1")
  )

lazy val aetherDeployRoot = (project in file("."))
  .aggregate(aetherDeploy, aetherDeploySigned)
  .settings(publish / skip := true)
