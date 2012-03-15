version := "0.1"

aether.AetherKeys.deployRepository  := "foo" at (file(".") / "target" / "repo").toURI.toURL.toString


seq(aether.Aether.aetherSettings: _*)
