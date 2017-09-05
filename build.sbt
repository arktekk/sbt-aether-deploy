organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies ++= {
  val mavenVersion = "3.5.0"
  val aetherVersion = "1.1.0"
  Seq(
    "javax.inject"              % "javax.inject"                  % "1"      % "provided",
    "org.codehaus.plexus"       % "plexus-component-annotations"  % "1.7.1"  % "provided",
  "org.sonatype.plexus"       % "plexus-sec-dispatcher"         % "1.4" exclude("org.codehaus.plexus", "plexus-utils"),
    "com.google.inject"         % "guice"                         % "4.0" exclude("com.google.guava", "guava"),
    "org.apache.maven"          % "maven-resolver-provider"       % mavenVersion,
    "org.codehaus.plexus"       % "plexus-interpolation"          % "1.24",
    "org.apache.maven"          % "maven-model"                   % mavenVersion,
    "org.apache.maven"          % "maven-core"                    % mavenVersion,
    "org.eclipse.aether"        % "aether-impl"                   % aetherVersion,
    "org.eclipse.aether"        % "aether-connector-basic"        % aetherVersion,
    "org.eclipse.aether"        % "aether-transport-http"         % aetherVersion exclude("org.apache.httpcomponents", "httpclient"),
    "org.eclipse.aether"        % "aether-transport-file"         % aetherVersion,
    "org.apache.maven.wagon"    % "wagon-provider-api"            % "2.12",
    "ch.qos.logback"            % "logback-classic"               % "1.2.2",
    "org.apache.httpcomponents" % "httpclient"                    % "4.5.3" exclude("commons-logging", "commons-logging")
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
