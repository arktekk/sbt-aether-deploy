organization := "no.arktekk.sbt"

description := "Deploy in SBT using Sonatype Aether"

name := "aether-deploy"

libraryDependencies ++= {
  val mavenVersion = "3.5.0"
  val mavenResolverVersion = "1.1.1"
  Seq(
    "org.apache.maven"          % "maven-resolver-provider"       % mavenVersion,
    "org.apache.maven.resolver" % "maven-resolver-api"            % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-impl"           % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-connector-basic"% mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-http" % mavenResolverVersion,
    "org.apache.maven.resolver" % "maven-resolver-transport-file" % mavenResolverVersion,
  )
}

//sbt-version specific dependencies
libraryDependencies ++= (CrossVersion partialVersion (sbtVersion in pluginCrossBuild).value match {
  case Some((0, 13)) => Seq(
    //sbt 0.13.x requires that an slf4j backend be present on the classpath.
    // This just directs all the basic logging stuff to a simple provider
    "org.slf4j" % "slf4j-simple" % "1.7.25"
  )
  case _ =>
    //sbt 1.0.0 and above provide an slf4j backend at runtime
    Seq.empty
})

scalacOptions := Seq("-deprecation", "-unchecked")

enablePlugins(SbtPlugin)
crossSbtVersions := Seq("1.2.6", "0.13.17")

scriptedLaunchOpts := { scriptedLaunchOpts.value ++
  Seq("-Xmx1024M", "-XX:MaxPermSize=256M", "-Dplugin.version=" + (version in ThisBuild).value)
}

scriptedBufferLog := false


libraryDependencies += {
  val sbtV = (sbtBinaryVersion in pluginCrossBuild).value
  val scalaV = (scalaBinaryVersion in pluginCrossBuild).value
  sbt.Defaults.sbtPluginExtra("com.jsuereth" % "sbt-pgp" % "1.1.2-1" % "provided", sbtV, scalaV)
}
