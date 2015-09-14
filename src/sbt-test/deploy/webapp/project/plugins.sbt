val pluginVersion = scala.util.Properties.propOrNone("plugin.version").getOrElse(
throw new RuntimeException("""
  |The system property 'plugin.version' is not defined.
  |Specify this property using the scriptedLaunchOpts -D.
""".stripMargin))

addSbtPlugin("com.earldouglas" % "xsbt-web-plugin" % "2.0.4")

addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % pluginVersion)
