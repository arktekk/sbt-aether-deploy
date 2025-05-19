package aether
package internal

import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.supplier.RepositorySystemSupplier
import org.eclipse.aether.{ConfigurationProperties, DefaultRepositorySystemSession, RepositorySystem}
import sbt.std.TaskStreams

import java.io.File
import scala.collection.JavaConverters.mapAsJavaMap
import scala.util.Try

object Booter {
  private def newRepositorySystem(): RepositorySystem = {
    val system = new RepositorySystemSupplier().get()
    system
  }

  private def init(
      localRepoDir: File,
      streams: TaskStreams[_],
      coordinates: MavenCoordinates
  ): (RepositorySystem, DefaultRepositorySystemSession) = {
    val system = newRepositorySystem()
    system -> newSession(system, localRepoDir, streams, coordinates)
  }

  def deploy(
      localRepoDir: File,
      streams: TaskStreams[_],
      coordinates: MavenCoordinates,
      customHeaders: Map[String, String],
      request: DeployRequest
  ): Try[Unit] = Try {
    val (system, session) = init(localRepoDir, streams, coordinates)
    if (customHeaders.nonEmpty) {
      session
        .setConfigProperty(ConfigurationProperties.HTTP_HEADERS + "." + request.getRepository.getId, mapAsJavaMap(customHeaders))
    }
    system.deploy(session, request)
  }

  def install(
      localRepoDir: File,
      streams: TaskStreams[_],
      coordinates: MavenCoordinates,
      request: InstallRequest
  ): Try[Unit] = Try {
    val (system, session) = init(localRepoDir, streams, coordinates)
    system.install(session, request)
  }

  private def newSession(implicit
      system: RepositorySystem,
      localRepoDir: File,
      streams: TaskStreams[_],
      coordinates: MavenCoordinates
  ): DefaultRepositorySystemSession = {
    val session   = new DefaultRepositorySystemSession()
    val localRepo = new LocalRepository(localRepoDir)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
    session.setTransferListener(new ConsoleTransferListener(streams.log))
    session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
    session.setUserProperties(mapAsJavaMap(coordinates.props))
    session.setProxySelector(SystemPropertyProxySelector.apply())
    session
  }
}
