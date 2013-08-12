# SBT aether deploy plugin
Deploys sbt-artifacts using Sonatype aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## project/plugins.sbt

```scala
...
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.10")
...
```


## Build file
  
```scala
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
```


## Override default publish/publish-local task

```scala
seq(aetherPublishSettings: _*)
```


## Add credentials

```scala
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
```

# Usage

To deploy to remote Maven repository.

    sbt aether-deploy

To deploy to local maven repository.

    sbt aether-install

# Usage if the publish/publish-local task is overriden

To deploy to remote Maven repository.

    sbt publish

To deploy to local maven repository.

    sbt publish-local

# Proxies

Documentation for proxies can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)

# Using the plugin with sbt-pgp-plugin 0.8

Previously the [sbt-pgp-plugin](https://github.com/sbt/sbt-pgp) hooked into the published-artifacts task, 
and this plugin does the same. This is no longer the case.

## Workaround until code is updated

```scala
seq(aetherSettings: _*)

aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, signedArtifacts in Compile) map {
  (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
    aether.Aether.createArtifact(artifacts, pom, coords, mainArtifact) 
}
```

This should now allow aether-deploy task to work with the sbt-pgp-plugin

## Overriding the publish-signed task

```scala
publishSigned <<= deploy
```
   
# Using .scala file

To use the plugin in a .scala file you have to import it like this:

```scala
import aether.Aether._
```
