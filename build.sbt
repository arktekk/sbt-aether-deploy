organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies ++= {
  val mavenVersion = "3.3.9"
  val aetherVersion = "1.1.0"
  Seq(
    aetherExclude("org.apache.maven"          % "maven-aether-provider"         % mavenVersion),
    "org.codehaus.plexus"       % "plexus-interpolation"          % "1.22",
    "org.apache.maven"          % "maven-model"                   % mavenVersion,
    "org.eclipse.aether"        % "aether-impl"                   % aetherVersion,
    "org.eclipse.aether"        % "aether-connector-basic"        % aetherVersion,
    "org.eclipse.aether"        % "aether-transport-http"         % aetherVersion exclude("org.apache.httpcomponents", "httpclient"),
    "org.eclipse.aether"        % "aether-transport-file"         % aetherVersion,
    "org.eclipse.aether"        % "aether-transport-wagon"        % aetherVersion exclude("org.apache.maven.wagon", "wagon-provider-api"),
    "org.apache.maven.wagon"    % "wagon-provider-api"            % "2.12",
    "ch.qos.logback"            % "logback-classic"               % "1.2.2",
    "org.apache.httpcomponents" % "httpclient"                    % "4.5.3" exclude("commons-logging", "commons-logging"),
    "javax.inject"              % "javax.inject"                  % "1"      % "provided",
    "org.codehaus.plexus"       % "plexus-component-annotations"  % "1.7.1"  % "provided"
  )
}

dependencyOverrides += "org.slf4j" % "jcl-over-slf4j" % "1.7.25"

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
  sbt.Defaults.sbtPluginExtra("com.jsuereth" % "sbt-pgp" % "1.1.0-M1" % "provided", sbtV, scalaV)
}

def aetherExclude(moduleId: ModuleID): ModuleID = {
  List("aether-api", "aether-spi", "aether-util", "aether-impl").foldLeft(moduleId)((m, s) => m.exclude("org.eclipse.aether", s))
}
