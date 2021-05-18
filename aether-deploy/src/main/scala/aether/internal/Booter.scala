package aether
package internal

import java.io.File

import aether.maven.SnapshotMetadataGeneratorFactory
import aether.maven.VersionsMetadataGeneratorFactory
import org.apache.maven.repository.internal.{DefaultArtifactDescriptorReader, DefaultVersionRangeResolver, DefaultVersionResolver}
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.{ConfigurationProperties, DefaultRepositorySystemSession, RepositorySystem}
import org.eclipse.aether.impl._
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.{LocalRepository, ProxySelector}
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.transport.http.HttpTransporterFactory

import scala.collection.JavaConverters.mapAsJavaMap
import sbt.std.TaskStreams

import scala.util.Try

object Booter {
  private def newRepositorySystem(): RepositorySystem = {
    val locator = new DefaultServiceLocator()
    locator.addService(classOf[RepositoryLayoutFactory], classOf[SbtPluginLayoutFactory])
    locator.addService(classOf[VersionResolver], classOf[DefaultVersionResolver])
    locator.addService(classOf[VersionRangeResolver], classOf[DefaultVersionRangeResolver])
    locator.addService(classOf[ArtifactDescriptorReader], classOf[DefaultArtifactDescriptorReader])
    locator.addService(
      classOf[RepositoryConnectorFactory],
      classOf[org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory]
    )
    locator.setServices(classOf[ProxySelector], SystemPropertyProxySelector())
    locator.addService(classOf[MetadataGeneratorFactory], classOf[SnapshotMetadataGeneratorFactory])
    locator.addService(classOf[MetadataGeneratorFactory], classOf[VersionsMetadataGeneratorFactory])

    addTransporterFactories(locator)

    locator.setErrorHandler(new DefaultServiceLocator.ErrorHandler {
      override def serviceCreationFailed(clazz: Class[_], impl: Class[_], exception: Throwable) {
        println("Service of type %s failed to be created by impl type %s".format(clazz, impl))
        exception.printStackTrace(System.err)
      }
    })

    val system: RepositorySystem = locator.getService(classOf[RepositorySystem])
    if (system == null) sys.error("Failed to create RepositorySystem. This cannot be good!")
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

  private def newSession(
      implicit system: RepositorySystem,
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
    session
  }

  private def addTransporterFactories(locator: DefaultServiceLocator) {
    val services = Seq(
      new HttpTransporterFactory(),
      new FileTransporterFactory().setPriority(10000f)
    )

    locator.setServices(classOf[TransporterFactory], services: _*)
  }
}
