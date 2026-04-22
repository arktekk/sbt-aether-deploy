package aether

import aether.AetherKeys._
import com.jsuereth.sbtpgp.{PgpKeys, SbtPgp}
import sbt._
import sbt.Keys.fileConverter
import sbtcompat.PluginCompat._
import xsbti.FileConverter

object SignedAetherPlugin extends AutoPlugin {
  override def trigger         = allRequirements
  override def requires        = AetherPlugin && SbtPgp
  override def projectSettings = Seq(
    aetherArtifact := Def.uncached {
      implicit val conv: FileConverter = fileConverter.value
      AetherPlugin.createArtifact(
        (Compile / PgpKeys.signedArtifacts).value,
        aetherCoordinates.value,
        aetherPackageMain.value
      )
    }
  )

  object autoImport {
    def overridePublishSignedSettings: Seq[Setting[_]]      = Seq(PgpKeys.publishSigned := aetherDeploy.value)
    def overridePublishSignedLocalSettings: Seq[Setting[_]] =
      Seq(PgpKeys.publishLocalSigned := {
        aetherInstall.value
        PgpKeys.publishLocalSigned.value
      })
    def overridePublishSignedBothSettings: Seq[Setting[_]]  = overridePublishSignedSettings ++ overridePublishSignedLocalSettings
  }

}
