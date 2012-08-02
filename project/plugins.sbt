
// Enable for IDEA plugin. Disabled since it depends on snapshot version
//resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"

// libraryDependencies <+= (sbtVersion, scalaVersion) { (sbtV, scalaV) => sbtV match {
//     case v if (v.startsWith("0.12")) => "com.github.mpeltonen" % "sbt-idea" % "1.1.0-SNAPSHOT" extra(CustomPomParser.SbtVersionKey -> "0.12", CustomPomParser.ScalaVersionKey -> scalaV)
    // case v if (v.startsWith("0.11")) => "com.github.mpeltonen" % "sbt-idea" % "1.1.0-SNAPSHOT" extra(CustomPomParser.SbtVersionKey -> sbtV, CustomPomParser.ScalaVersionKey -> scalaV) cross(false)
    // case _ => error("Not supported")
  // }  
// }

// Enabled for 0.11.x support
resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

libraryDependencies <+= (sbtVersion) { sv => sv match {
    case v if (v.startsWith("0.12")) => "org.scala-sbt" % "scripted-plugin" % sv
    case "0.11.3" => "org.scala-sbt" %% "scripted-plugin" % sv
    case "0.11.2" => "org.scala-tools.sbt" % "scripted-plugin" % sv
    case _ => error("Not supported")
  }  
}

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
