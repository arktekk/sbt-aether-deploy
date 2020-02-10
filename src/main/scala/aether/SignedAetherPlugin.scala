package aether

import com.jsuereth.sbtpgp.PgpKeys
import sbt._, Keys._
import AetherKeys._

object SignedAetherPlugin extends AetherPlugin {
  override def trigger  = noTrigger
  override def requires = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(
    aetherArtifact := {
      createArtifact((PgpKeys.signedArtifacts in Compile).value, aetherCoordinates.value, aetherPackageMain.value)
    }
  )

  object autoImport {
    def overridePublishSignedSettings: Seq[Setting[_]] = Seq(PgpKeys.publishSigned := aetherDeploy.value)
    def overridePublishSignedLocalSettings: Seq[Setting[_]] =
      Seq(PgpKeys.publishLocalSigned := {
        aetherInstall.value
        PgpKeys.publishLocalSigned.value
      })
    def overridePublishSignedBothSettings: Seq[Setting[_]] = overridePublishSignedSettings ++ overridePublishSignedLocalSettings
  }

}
