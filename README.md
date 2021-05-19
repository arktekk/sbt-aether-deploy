# SBT aether deploy plugin
Deploys sbt-artifacts using Maven Artifact Provider. 

The same behaviour as Maven should be expected.

## project/plugins.sbt

```scala
addSbtPlugin("no.arktekk.sbt" % "aether-deploy" % "0.27.0")

/** OR **/

addSbtPlugin("no.arktekk.sbt" % "aether-deploy-signed" % "0.27.0") // For sbt-pgp 2.x support
```

# Breaking Changes

## 0.27.0
`aether-deploy-signed` now uses the new maven coordinates for `sbt-pgp`

## 0.25.0
`sbt-pgp` support now published separately, and requires `aether-deploy-signed` dependency declaration instead of
`aether-deploy` to support zero configuration use of `SignedAetherPlugin`.

## 0.24.0
If you want to use `sbt-pgp` you need to use version `2.0.1` or higher.

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
## Overriding the publish-signed task _(applies to 'aether-deploy-signed' only)_

```scala
overridePublishSignedSettings
```

## Overriding the publish-signed-local task _(applies to 'aether-deploy-signed' only)_

```scala
overridePublishSignedLocalSettings
```

## Overriding the publish-signed and publish-signed-local task _(applies to 'aether-deploy-signed' only)_

```scala
overridePublishSignedBothSettings
```

## Add credentials

```scala
credentials += Credentials(Path.userHome / ".sbt" / ".credentials")
```

# Usage

To deploy to remote Maven repository.

    sbt aetherDeploy

To deploy to local maven repository.

    sbt aetherInstall

# Usage if the publish/publish-local task is overriden

To deploy to remote Maven repository.

    sbt publish

To deploy to local maven repository.

    sbt publishLocal

# Proxies

Documentation for proxies can be found [here](http://docs.oracle.com/javase/6/docs/technotes/guides/net/proxies.html)

# Using the plugin with sbt-pgp-plugin 1.1.2-1 or higher and aether-deploy 0.24.0 or below

You will need to add the sbt-pgp-plugin as described [here](https://github.com/sbt/sbt-pgp).

```scala
enablePlugins(SignedAetherPlugin) // Only required for 0.24.0 and below. SignedAetherPlugin is
                                  // automatically enabled if sbt-pgp is enabled on the project.

disablePlugins(AetherPlugin) // Only required for 0.24.0 and below.

```

This should now allow aether-deploy task to work with the sbt-pgp-plugin
