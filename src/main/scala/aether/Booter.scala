package aether

import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.{DefaultRepositorySystemSession, RepositorySystemSession, RepositorySystem}
import java.io.File
import org.eclipse.aether.transport.wagon.{WagonTransporterFactory, WagonConfigurator, WagonProvider}
import org.eclipse.aether.transport.file.FileTransporterFactory
import org.eclipse.aether.impl.DefaultServiceLocator
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.spi.connector.layout.RepositoryLayoutFactory

import sbt.std.TaskStreams

object Booter {
  def newRepositorySystem(wagons: Seq[WagonWrapper]) = {
    val locator = new DefaultServiceLocator()
    //locator.setServices(classOf[Logger], new Slf4jLogger(LoggerFactory.getLogger("aether")) )
    val wagonRepositoryConnectorFactory = new WagonTransporterFactory()
    wagonRepositoryConnectorFactory.setPriority(1000)
    val httpTransporterFactory = new HttpTransporterFactory()
    httpTransporterFactory.setPriority(0)
    val fileRepositoryConnectorFactory = new FileTransporterFactory()
    fileRepositoryConnectorFactory.setPriority(100000)
    locator.addService(classOf[RepositoryLayoutFactory], classOf[SbtPluginLayoutFactory])
    locator.setServices(classOf[WagonProvider], new ExtraWagonProvider(wagons))
    locator.setServices(classOf[WagonConfigurator], NoOpWagonConfigurator)
    locator.setServices(classOf[TransporterFactory], wagonRepositoryConnectorFactory, fileRepositoryConnectorFactory, httpTransporterFactory)
    wagonRepositoryConnectorFactory.initService(locator)
    fileRepositoryConnectorFactory.initService(locator)
    httpTransporterFactory.initService(locator)
    locator.getService(classOf[RepositorySystem])
  }

  def newSession(implicit system: RepositorySystem, localRepoDir: File, streams: TaskStreams[_]): RepositorySystemSession = {
    val session = new DefaultRepositorySystemSession()
    val localRepo = new LocalRepository(localRepoDir)
   // session.setProxySelector(SystemPropertyProxySelector)
    session.setLocalRepositoryManager(system.newLocalRepositoryManager(session, localRepo))
    session.setTransferListener(new ConsoleTransferListener(streams.log))
    session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
    session
  }
}
