# SBT aether deploy plugin
Deploys sbt-artifacts using Eclipse aether. 
Aether is the same library as maven itself uses, meaning that the same behaviour should be expected.

## project/plugins.sbt

```scala
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.20.0")
```

# Possible Breaking Changes

## 0.18

The version to be used by the aetherCoordinates will be scoped using ThisBuild, to work better with the release plugin.

To get the old behaviour you will need to add this to your `build.sbt`:
 
```scala
aetherOldVersionMethod := true
```

Logging level of progress has been changed from info to debug.
 
You can turn off the progress logging by adding this to your `build.sbt`:

```scala
import aether.AetherKeys._

logLevel in aetherDeploy := Level.Info
```

## Caveat
If you see errors similar to what is described in [this ticket](https://github.com/arktekk/sbt-aether-deploy/issues/25) 
then you might want to check if you are using a global plugin. 

There are known incompabilities with `sbt-pgp` if sbt-pgp is used as a global plugin.


## Build file
  
```scala
publishTo := {
  if ((version in ThisBuild).value.endsWith("SNAPSHOT")) {
    Some(Opts.resolver.sonatypeSnapshots)
  } else {
    Some(Opts.resolver.sonatypeStaging)
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

# Using the plugin with sbt-pgp-plugin 1.1.0-M1 or higher

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
