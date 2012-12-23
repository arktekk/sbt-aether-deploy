# SBT aether deploy plugin
Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## Caveat 
This plugin should not yet be used for publishing sbt plugins. There are an experimental branch for making this work.

## project/plugins.sbt

	...
	addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.7")
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

# Proxies

Documentation for proxies can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)