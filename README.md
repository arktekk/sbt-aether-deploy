# SBT aether deploy plugin

Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## project/plugins.sbt

	...
	addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.6")
	...


## Build file
	
      	publishTo <<= (version: String) {
	  if (version.endsWith("SNAPSHOT") {
	    Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
	  }
          else {
	    Some("Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
	  }
	} 

	seq(aetherSettings: _*)


## Override default publish task

	seq(aetherPublishSettings: _*)


## Add credentials

	credentials += Credentials(Path.userHome / ".sbt" / ".credentials")

# Usage

	sbt aether-deploy

# Usage if the publish task is overriden

	sbt publish
