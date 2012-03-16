# SBT aether deploy plugin

## project/plugins.sbt

	...
	addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.1")
	...


## Build file
	
	aether.AetherKeys.deployRepository  <<= (version: String) {
	  if (version.endsWith("SNAPSHOT") {
	    "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
	  }
          else {
	     "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
	  }
	} 

	seq(aether.Aether.aetherSettings: _*)

# Usage

# Usage

	sbt aether-deployy
