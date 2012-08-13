package aether

import org.sonatype.aether.repository.LocalRepository
import org.sonatype.aether.{RepositorySystemSession, RepositorySystem}
import java.io.File
import org.sonatype.aether.spi.connector.RepositoryConnectorFactory
import org.apache.maven.wagon.Wagon
import org.sonatype.aether.connector.wagon.{WagonRepositoryConnectorFactory, WagonProvider}
import org.sonatype.aether.connector.file.FileRepositoryConnectorFactory
import org.apache.maven.repository.internal.{MavenServiceLocator, MavenRepositorySystemSession}
import sbt.std.TaskStreams
import org.sonatype.aether.connector.async.AsyncRepositoryConnectorFactory

object Booter {
  def newRepositorySystem(wagons: Seq[WagonWrapper]) = {
    val locator = new MavenServiceLocator()
    locator.addService(classOf[RepositoryConnectorFactory], classOf[FileRepositoryConnectorFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[AsyncRepositoryConnectorFactory])
    locator.addService(classOf[RepositoryConnectorFactory], classOf[WagonRepositoryConnectorFactory])
    locator.setServices(classOf[WagonProvider], new ExtraWagonProvider(wagons))
    locator.getService(classOf[RepositorySystem])
  }

  def newSession(implicit system: RepositorySystem, localRepoDir: File, streams: TaskStreams[_]): RepositorySystemSession = {
      val session = new MavenRepositorySystemSession()

      val localRepo = new LocalRepository(localRepoDir)
      session.setLocalRepositoryManager(system.newLocalRepositoryManager(localRepo))
      session.setTransferListener(new ConsoleTransferListener(streams.log))
      session.setRepositoryListener(new ConsoleRepositoryListener(streams.log))
      session
  }

  private class ExtraWagonProvider(wagons: Seq[WagonWrapper]) extends WagonProvider {
    private val map = wagons.map(w => w.scheme -> w.wagon).toMap

    def lookup(roleHint: String ): Wagon = {
      map.get(roleHint).getOrElse(throw new IllegalArgumentException("Unknown wagon type"))
    }

    def release(wagon: Wagon){
      try {
        if (wagon != null) wagon.disconnect()
      }
      catch {
        case e:Exception => e.printStackTrace()
      }
    }
  }
}
