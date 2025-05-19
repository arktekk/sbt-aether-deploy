val pluginVersion = scala.util.Properties.propOrNone("plugin.version").getOrElse(
  throw new RuntimeException("""
                               |The system property 'plugin.version' is not defined.
                               |Specify this property using the scriptedLaunchOpts -D.
                             """.stripMargin))

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % pluginVersion)

addSbtPlugin("com.github.sbt" % "sbt-native-packager" % "1.11.1")
