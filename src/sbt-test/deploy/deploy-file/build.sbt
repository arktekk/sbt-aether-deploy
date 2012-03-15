version := "0.1"

name := "deploy-file"

organization := "deploy-file"

aether.AetherKeys.deployRepository  := "foo" at (file(".") / "target" / "repo").toURI.toURL.toString


seq(aether.Aether.aetherSettings: _*)
