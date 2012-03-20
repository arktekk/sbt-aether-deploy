version := "0.1"

name := "deploy-file"

organization := "deploy-file"

deployRepository  := "foo" at (file(".") / "target" / "repo").toURI.toURL.toString

seq(aetherSettings: _*)

useGpg := true

gpgCommand := "gpg2"