# SBT aether deploy plugin
Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## Caveat 
This plugin should not yet be used for publishing sbt plugins. There are an experimental branch for making this work.

## project/plugins.sbt

  ...
  addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.8")
  ...


## Build file
  
  import aether.Aether._
    
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

# Using the plugin with sbt-pgp-plugin 0.8

Previously the [sbt-pgp-plugin](https://github.com/sbt/sbt-pgp) hooked into the published-artifacts task, 
and this plugin does the same. This is no longer the case.

## Workaround until code is updated

  seq(aetherSettings: _*)

  aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, signedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) => {
      val subartifacts = artifacts.filterNot{case (a, f) => a.classifier == None && !a.extension.contains("asc")}
      val actualSubArtifacts = AetherSubArtifact(pom, None, "pom") +: subartifacts.foldLeft(Vector[AetherSubArtifact]()){case (seq, (a, f)) => AetherSubArtifact(f, a.classifier, a.extension) +: seq}
      val actualCoords = coords.copy(extension = getActualExtension(mainArtifact))
      AetherArtifact(mainArtifact, actualCoords, actualSubArtifacts)
    }
  }

This should now allow aether-deploy task to work with the sbt-pgp-plugin

## Overriding the publish-signed task

  publishSigned <<= deploy
   
   
## Using .scala file

To use the plugin in a .scala file you have to import it like this:

  import aether.Aether._
