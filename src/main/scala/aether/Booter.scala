package aether

import org.sonatype.aether.repository.LocalRepository
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import java.io.File
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.sonatype.aether.connector.wagon.{WagonConfigurator, WagonRepositoryConnectorFactory, WagonProvider}
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import sbt.std.TaskStreams
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory
import org.sonatype.aether.spi.log.Logger
import org.sonatype.aether.impl.internal.Slf4jLogger
import org.slf4j.LoggerFactory

object Booter {
  def newRepositorySystem(wagons: Seq[WagonWrapper]) = {
    val locator = new MavenServiceLocator()
    locator.setServices(classOf[Logger], new Slf4jLogger(LoggerFactory.getLogger("aether")) )
    val wagonRepositoryConnectorFactory = new WagonRepositoryConnectorFactory()
    wagonRepositoryConnectorFactory.setPriority(1000)
    val asyncRepositoryConnectorFactory = new AsyncRepositoryConnectorFactory()
    asyncRepositoryConnectorFactory.setPriority(0)
    val fileRepositoryConnectorFactory = new FileRepositoryConnectorFactory()
    fileRepositoryConnectorFactory.setPriority(100000)
    locator.setServices(classOf[WagonProvider], new ExtraWagonProvider(wagons))
    locator.setServices(classOf[WagonConfigurator], NoOpWagonConfigurator)
    locator.setServices(classOf[RepositoryConnectorFactory], wagonRepositoryConnectorFactory, fileRepositoryConnectorFactory, asyncRepositoryConnectorFactory)
    wagonRepositoryConnectorFactory.initService(locator)
    fileRepositoryConnectorFactory.initService(locator)
    asyncRepositoryConnectorFactory.initService(locator)
    locator.getService(classOf[RepositorySystem])
  }

  def newSession(implicit system: RepositorySystem, localRepoDir: File, streams: TaskStreams[_]): RepositorySystemSession = {
    val localRepo = new LocalRepository(localRepoDir)
    val session = new MavenRepositorySystemSession()
    session.setProxySelector(SystemPropertyProxySelector)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo))
    session.setTransferListener(new ConsoleTransferListener(streams.log))
    session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
    session
  }
}
