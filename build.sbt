organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies <++= (target).apply{ (target) =>
  val mavenVersion = "3.2.1"
  val aetherVersion = "0.9.0.v20140226"
  Seq(
    ExcludeAllTransitiveDeps(target, "org.apache.maven" % "maven-aether-provider" % mavenVersion),
    ExcludeAllTransitiveDeps(target, "org.apache.maven" % "maven-model-builder" % mavenVersion),
    ExcludeAllTransitiveDeps(target, "org.apache.maven" % "maven-repository-metadata" % mavenVersion),
    ExcludeAllTransitiveDeps(target, "org.codehaus.plexus" % "plexus-interpolation" % "1.19"),
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

scalacOptions := Seq("-deprecation", "-unchecked")

sbtPlugin := true

CrossBuilding.crossSbtVersions := Seq("0.13")

ScriptedPlugin.scriptedSettings

net.virtualvoid.sbt.cross.CrossPlugin.crossBuildingSettings
