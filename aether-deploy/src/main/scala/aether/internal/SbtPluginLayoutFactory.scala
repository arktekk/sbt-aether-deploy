package aether
package internal

import org.eclipse.aether.spi.connector.layout.{RepositoryLayout, RepositoryLayoutFactory}
import org.eclipse.aether.RepositorySystemSession
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.transfer.NoRepositoryLayoutException
import org.eclipse.aether.metadata.Metadata
import org.eclipse.aether.spi.connector.layout.RepositoryLayout.Checksum
import org.eclipse.aether.artifact.Artifact
import org.eclipse.aether.metadata.Metadata.Nature

import java.net.URI

import scala.collection.JavaConverters.mapAsScalaMap

class SbtPluginLayoutFactory extends RepositoryLayoutFactory {
  def newInstance(session: RepositorySystemSession, repository: RemoteRepository): RepositoryLayout = {
    import MavenCoordinates._
    val userProperties = mapAsScalaMap(session.getUserProperties)
    (repository.getContentType, userProperties.get(SbtVersion), userProperties.get(ScalaVersion)) match {
      case ("sbt-plugin", Some(sbtVersion), Some(scalaVersion)) => new SbtRepositoryLayout(sbtVersion, scalaVersion)
      case _                                                    => throw new NoRepositoryLayoutException(repository, "Not an sbt-plugin repository")
    }
  }

  def getPriority: Float = 100.0f
}

class SbtRepositoryLayout(sbtVersion: String, scalaVersion: String) extends RepositoryLayout {

  def getLocation(artifact: Artifact, upload: Boolean): URI = {
    val path = new StringBuilder(128)
    path.append(artifact.getGroupId.replace('.', '/')).append('/')
    path.append(artifact.getArtifactId).append('_').append(scalaVersion).append('_').append(sbtVersion).append('/')
    path.append(artifact.getBaseVersion).append('/')
    path.append(artifact.getArtifactId).append('-').append(artifact.getVersion)
    if (artifact.getClassifier != null && artifact.getClassifier.trim.nonEmpty) {
      path.append("-").append(artifact.getClassifier)
    }
    if (artifact.getExtension.nonEmpty) {
      path.append('.').append(artifact.getExtension)
    }
    URI.create(path.toString())
  }

  def getLocation(metadata: Metadata, upload: Boolean): URI = {
    val path = new StringBuilder(128)
    if (metadata.getGroupId.nonEmpty) {
      path.append(metadata.getGroupId.replace('.', '/')).append('/')
      if (metadata.getArtifactId.nonEmpty) {
        path.append(metadata.getArtifactId)

        // only append the scala and sbt versions for non-snapshots metadata objects
        // as they will already have a transformed artifact id.
        // this avoids something like:
        // foo_2.12_1.0_2.12_1.0
        val nature = metadata.getNature
        if(nature == Nature.RELEASE || nature == Nature.RELEASE_OR_SNAPSHOT) {
          path.append('_').append(scalaVersion).append('_').append(sbtVersion)
        }

        path.append('/')

        if (metadata.getVersion.nonEmpty) path.append(metadata.getVersion).append('/')
      }
    }
    path.append(metadata.getType)
    URI.create(path.toString())
  }

  def getChecksums(artifact: Artifact, upload: Boolean, location: URI): java.util.List[Checksum] = {
    java.util.Arrays.asList(Checksum.forLocation(location, "SHA-1"), Checksum.forLocation(location, "MD5"))
  }

  def getChecksums(metadata: Metadata, upload: Boolean, location: URI): java.util.List[Checksum] =
    java.util.Collections.emptyList()
}
