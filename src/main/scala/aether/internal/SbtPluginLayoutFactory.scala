package aether
package internal

import org.eclipse.aether.spi.connector.layout.{RepositoryLayout, RepositoryLayoutFactory}
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.transfer.NoRepositoryLayoutException
import org.eclipse.aether.metadata.Metadata
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.Checksum
import org.eclipse.aether.artifact.Artifact

import java.net.URI

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
    import MavenCoordinates._
    val sbtVersion = artifact.getProperties.get(SbtVersion)
    val scalaVersion = artifact.getProperties.get(ScalaVersion)
    val path = new StringBuilder(128)
    path.append(artifact.getGroupId.replace('.', '/')).append('/')
    path.append(artifact.getArtifactId).append('_').append(scalaVersion).append('_').append(sbtVersion).append('/')
    path.append(artifact.getBaseVersion).append('/')
    path.append(artifact.getArtifactId).append('-').append(artifact.getVersion)
    if (artifact.getClassifier != null && !artifact.getClassifier.trim.isEmpty) {
      path.append("-").append(artifact.getClassifier)
    }
    if (artifact.getExtension.length > 0) {
      path.append('.').append(artifact.getExtension)
    }
    URI.create(path.toString())
  }

  def getLocation(metadata: Metadata, upload: Boolean): URI = null

  def getChecksums(artifact: Artifact, upload: Boolean, location: URI): java.util.List[Checksum] = {
    java.util.Arrays.asList(Checksum.forLocation(location, "SHA-1"), Checksum.forLocation(location, "MD5"))
  }

  def getChecksums(metadata: Metadata, upload: Boolean, location: URI): java.util.List[Checksum] = java.util.Collections.emptyList()
}
