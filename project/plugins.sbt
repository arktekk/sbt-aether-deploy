
libraryDependencies <+= sbtVersion("org.scala-sbt" % "scripted-plugin" % _)

addSbtPlugin("com.jsuereth" % "sbt-pgp" % "1.0.0")

addSbtPlugin("net.virtual-void" % "sbt-dependency-graph" % "0.8.2")

addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.0.0-M15")
