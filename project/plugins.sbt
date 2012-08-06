resolvers <++= sbtVersion(sv => sv match {
 case v if (v.startsWith("0.11")) => Seq(
      Resolver.url("scalasbt", new URL("http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases"))(Resolver.ivyStylePatterns),
      Resolver.url("Typesafe repository", new java.net.URL("http://typesafe.artifactoryonline.com/typesafe/ivy-releases/"))(Resolver.defaultIvyPatterns)
    )
 case _ => Nil
})


libraryDependencies <+= (sbtVersion) { sv => sv match {
    case v if (v.startsWith("0.12")) => "org.scala-sbt" % "scripted-plugin" % sv
    case "0.11.3" => "org.scala-sbt" %% "scripted-plugin" % sv
    case "0.11.2" => "org.scala-tools.sbt" %% "scripted-plugin" % sv
    case _ => error("Not supported")
  }  
}

addSbtPlugin("com.github.mpeltonen" % "sbt-idea" % "1.1.0")

addSbtPlugin("com.jsuereth" % "xsbt-gpg-plugin" % "0.6")
