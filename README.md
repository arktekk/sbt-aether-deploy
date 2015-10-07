# SBT aether deploy plugin
Deploys sbt-artifacts using Eclipse aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## project/plugins.sbt

```scala
...
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.16")
...
```

## Caveat
If you see errors similar to what is described in this ticket https://github.com/arktekk/sbt-aether-deploy/issues/25 then you might want to check if you are using a global plugin. 

There are known incompabilities with `sbt-pgp` if sbt-pgp is used as a global plugin.


## Build file
  
```scala
publishTo <<= (version: String) {
  if (version.endsWith("SNAPSHOT")) {
    Some("Sonatype Nexus Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots")
  } else {
    Some("Sonatype Nexus Staging" at "https://oss.sonatype.org/service/local/staging/deploy/maven2")
  }
}
```

This plugin is now an Autoplugin, so there is no need to add extra config to make it work.


## Override default publish task

```scala
overridePublishSettings
```

## Override default publish-local task

```scala
overridePublishLocalSettings
```

## Override both publish and publish-local task
```scala
overridePublishBothSettings
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

# Using the plugin with sbt-pgp-plugin 1.0 or higher

You will need to add the sbt-pgp-plugin as described [here](https://github.com/sbt/sbt-pgp).

```scala
enablePlugins(SignedAetherPlugin)

disablePlugins(AetherPlugin)

```

This should now allow aether-deploy task to work with the sbt-pgp-plugin

## Overriding the publish-signed task

```scala
overridePublishSignedSettings
```
## Overriding the publish-signed-local task

```scala
overridePublishSignedLocalSettings
```

## Overriding the publish-signed and publish-signed-local task

```scala
overridePublishSignedBothSettings
```
