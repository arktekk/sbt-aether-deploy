# SBT aether deploy plugin

Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.
NOTE: Does not support adding gpg-signatures by default; need to figure out how that works. 

There is support for adding arbitrary artifacts via the attachedArtifacts setting.

## project/plugins.sbt

	...
	addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.3")
	...


## Build file
	
	deployRepository  <<= (version: String) {
	  if (version.endsWith("SNAPSHOT") {
	    "Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
	  }
          else {
	     "Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2"
	  }
	} 

	seq(aetherSettings: _*)

# Usage

# Usage

	sbt aether-deploy
