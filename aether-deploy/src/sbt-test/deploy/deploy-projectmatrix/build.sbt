version  := "0.1.0"
organization := "deploy-file"

scalaVersion := "3.8.3"

val projectMatrixScripted =
  projectMatrix.in(file("."))
    .jvmPlatform(scalaVersions = Seq("3.8.3"))


publishTo  := Some("foo" at (file(".") / "target" / "repo").toURI.toURL.toString)

