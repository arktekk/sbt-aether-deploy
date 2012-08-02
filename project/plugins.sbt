
resolvers += "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/"


//addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.0.0")

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0-SNAPSHOT", sbtVersion = "0.12")

resolvers += Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)

libraryDependencies <+= (sbtVersion) { sv => sv match {
    case "0.12.0" => "org.scala-sbt" % "scripted-plugin" % sv
    case "0.11.3" => "org.scala-sbt" %% "scripted-plugin" % sv
    case "0.11.2" => "org.scala-tools.sbt" % "scripted-plugin" % sv
    case _ => error("Not supported")
  }  
}

resolvers += Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns)

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
