package aether

import org.eclipse.aether.spi.connector.layout.{RepositoryLayout, RepositoryLayoutFactory}
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.transfer.NoRepositoryLayoutException
import org.eclipse.aether.metadata.Metadata
import java.net.URI
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.Checksum
import org.eclipse.aether.artifact.Artifact

class SbtPluginLayoutFactory extends RepositoryLayoutFactory {
  def newInstance(session: RepositorySystemSession, repository: RemoteRepository): RepositoryLayout = {
    repository.getContentType match {
      case "sbt-plugin" => SbtRepositoryLayout
      case _ => throw new NoRepositoryLayoutException(repository, "Not an sbt-plugin repository")
    }
  }

  def getPriority: Float = 100.0f
}

object SbtRepositoryLayout extends RepositoryLayout {

  def getLocation(artifact: Artifact, upload: Boolean): URI = {
    val groupId = artifact.getGroupId.split("\\.").toList
    val artifactId = artifact.getArtifactId
    val sbtVersion = artifact.getProperty("sbt.version", "")
    val scalaVersion = artifact.getProperty("scala.version", "")
    URI.create((groupId ::: List(artifactId)).mkString("/"))
  }

  def getLocation(metadata: Metadata, upload: Boolean): URI = {
    null
  }

  def getChecksums(artifact: Artifact, upload: Boolean, location: URI): java.util.List[Checksum] = {
    getChecksums(getLocation(artifact, upload))
  }

  def getChecksums(metadata: Metadata, upload: Boolean, location: URI): java.util.List[Checksum] = {
    getChecksums(getLocation(metadata, upload))
  }

  private def getChecksums(location: URI): java.util.List[Checksum] = {
    java.util.Arrays.asList(Checksum.forLocation(location, "SHA-1"), Checksum.forLocation(location, "MD5"))
  }
}