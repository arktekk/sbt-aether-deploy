package aether

import com.typesafe.sbt.pgp.PgpKeys
import sbt._, Keys._
import AetherKeys._

object SignedAetherPlugin extends AetherPlugin {
  override def trigger = noTrigger
  override def requires = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(signedArtifact)

  object autoImport {
    def overridePublishSignedSettings: Seq[Setting[_]] = Seq(PgpKeys.publishSigned <<= aetherDeploy)
    def overridePublishSignedLocalSettings: Seq[Setting[_]] = Seq(PgpKeys.publishLocalSigned <<= aetherInstall.dependsOn(PgpKeys.publishLocalSigned))
    def overridePublishSignedBothSettings: Seq[Setting[_]] = overridePublishSignedSettings ++ overridePublishSignedLocalSettings
  }


  def signedArtifact = aetherArtifact <<= (aetherCoordinates, aetherPackageMain, PgpKeys.signedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, artifacts: Map[Artifact, File]) =>
      createArtifact(artifacts, coords, mainArtifact)
  }
}
