organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies ++= {
  val mavenVersion = "3.5.0"
  val mavenResolverVersion = "1.1.1"
  Seq(
    "javax.inject"              % "javax.inject"                  % "1"      % "provided",
    "org.codehaus.plexus"       % "plexus-component-annotations"  % "1.7.1"  % "provided",
    "org.sonatype.plexus"       % "plexus-sec-dispatcher"         % "1.4" exclude("org.codehaus.plexus", "plexus-utils"),
    "com.google.inject"         % "guice"                         % "4.0" exclude("com.google.guava", "guava"),
    "org.apache.maven"          % "maven-resolver-provider"       % mavenVersion,
    "org.codehaus.plexus"       % "plexus-interpolation"          % "1.24",
    "org.apache.maven"          % "maven-model"                   % mavenVersion,
    "org.apache.maven"          % "maven-core"                    % mavenVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-connector-basic"% mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-http" % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
    "org.apache.maven.wagon"    % "wagon-provider-api"            % "2.12",
    "ch.qos.logback"            % "logback-classic"               % "1.2.2",
    "commons-logging"           % "commons-logging"               % "1.2"
  )
}

scalacOptions := Seq("-deprecation", "-unchecked")

sbtPlugin := true

ScriptedPlugin.scriptedSettings

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + (version in ThisBuild).value)
}

scriptedBufferLog := false


libraryDependencies += {
  val sbtV = (sbtBinaryVersion in pluginCrossBuild).value
  val scalaV = (scalaBinaryVersion in pluginCrossBuild).value
  sbt.Defaults.sbtPluginExtra("com.jsuereth" % "sbt-pgp" % "1.1.0" % "provided", sbtV, scalaV)
}
