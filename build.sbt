organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies ++= {
  val mavenVersion = "3.3.9"
  val aetherVersion = "1.1.0"
  Seq(
    "org.apache.maven" % "maven-aether-provider" % mavenVersion excludeAll(ExclusionRule("org.eclipse.aether")),
    "org.codehaus.plexus" % "plexus-interpolation" % "1.22" excludeAll(ExclusionRule("*", "*")),
    "org.apache.maven" % "maven-model" % mavenVersion,
    //"org.glassfish.hk2.external" % "javax.inject" % "2.2.0-b14" % "provided",
    //"org.codehaus.plexus" % "plexus-component-annotations" % "1.5.5" % "provided",
    "org.eclipse.aether" % "aether-impl" % aetherVersion,
    "org.eclipse.aether" % "aether-connector-basic" % aetherVersion,
    "org.eclipse.aether" % "aether-transport-http" % aetherVersion,
    "org.eclipse.aether" % "aether-transport-file" % aetherVersion,
    "org.eclipse.aether" % "aether-transport-wagon" % aetherVersion,
    "ch.qos.logback" % "logback-classic" % "1.0.13"
  )
}

dependencyOverrides += "org.slf4j" % "jcl-over-slf4j" % "1.7.5"

excludeDependencies += "com.google.guava" % "guava"
excludeDependencies += "org.apache.commons" % "commons-lang3"
excludeDependencies += "org.codehaus.plexus" % "plexus-component-annotations"
excludeDependencies += "org.codehaus.plexus" % "plexus-utils"

scalacOptions := Seq("-deprecation", "-unchecked")

publishMavenStyle := false

sbtPlugin := true

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + (version in ThisBuild).value)
}

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0" % "provided")

graphSettings
