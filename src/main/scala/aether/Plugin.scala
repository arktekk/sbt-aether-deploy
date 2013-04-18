package aether

import sbt._
import sbt.Keys._
import java.util.Collections
import org.sonatype.aether.util.artifact.{SubArtifact, DefaultArtifact}
import org.sonatype.aether.deployment.DeployRequest
import org.sonatype.aether.installation.InstallRequest
import org.sonatype.aether.repository.{Authentication, RemoteRepository}
import java.net.URI

object Aether extends sbt.Plugin {
  lazy val aetherArtifact = TaskKey[AetherArtifact]("aether-artifact", "Main artifact")
  lazy val coordinates = SettingKey[MavenCoordinates]("aether-coordinates", "The maven coordinates to the main artifact. Should not be overridden")
  lazy val wagons = SettingKey[Seq[WagonWrapper]]("aether-wagons", "The configured extra maven wagon wrappers.")
  lazy val deploy = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  lazy val install = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")

  lazy val aetherSettings: Seq[Setting[_]] = Seq(
    defaultWagons,
    defaultCoordinates,
    defaultArtifact,
    deployTask,
    installTask
  )

  lazy val aetherPublishSettings: Seq[Setting[_]] = aetherSettings ++ Seq(publish <<= deploy, publishLocal <<= install.dependsOn(publishLocal))

  lazy val defaultCoordinates = coordinates <<= (organization, name, version, scalaBinaryVersion, crossPaths, sbtPlugin).apply{
    (o, n, v, scalaV, crossPath, plugin) => {
      if (plugin) {
        sys.error("SBT is using maven incorrectly, meaning you will have to use sbt publish for sbt-plugins")
      }
      val aId = if (crossPath) "%s_%s".format(n, scalaV) else n
      MavenCoordinates(o, aId, v, None)
    }
  }

  lazy val defaultWagons = wagons := Seq.empty
  
  lazy val defaultArtifact = aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, packagedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
      createArtifact(artifacts, pom, coords, mainArtifact)
  }

  lazy val deployTask = deploy <<= (publishTo, wagons, credentials, aetherArtifact, streams).map{
    (repo: Option[Resolver], wag: Seq[WagonWrapper], cred: Seq[Credentials], artifact: AetherArtifact, s: TaskStreams) => {
      val repository = repo.collect{
        case x: MavenRepository => x
        case _ => sys.error("The configured repo MUST be a maven repo")
      }.getOrElse(sys.error("There MUST be a configured publish repo"))
      val maybeCred = scala.util.control.Exception.allCatch.apply {
        val href = URI.create(repository.root)
        val c = Credentials.forHost(cred, href.getHost)
        if (c.isEmpty) {
           s.log.warn("No credentials supplied for %s".format(href.getHost))
        }
        c
      }

      deployIt(artifact, wag, repository, maybeCred)(s)
    }}

  lazy val installTask = install <<= (wagons, aetherArtifact, streams).map{
    (wag: Seq[WagonWrapper], artifact: AetherArtifact, s: TaskStreams) => {
      installIt(artifact, wag)(s)
    }}

  def createArtifact(artifacts: Map[Artifact, sbt.File], pom: sbt.File, coords: MavenCoordinates, mainArtifact: sbt.File): AetherArtifact = {
    val subartifacts = artifacts.filterNot {
      case (a, f) => a.classifier == None && !a.extension.contains("asc")
    }
    val actualSubArtifacts = AetherSubArtifact(pom, None, "pom") +: subartifacts.foldLeft(Vector[AetherSubArtifact]()) {
      case (seq, (a, f)) => AetherSubArtifact(f, a.classifier, a.extension) +: seq
    }
    val actualCoords = coords.copy(extension = getActualExtension(mainArtifact))
    AetherArtifact(mainArtifact, actualCoords, actualSubArtifacts)
  }

  private def getActualExtension(file: File) = {
    val name = file.getName
    name.substring(name.lastIndexOf('.') + 1)
  }
    
  private def toRepository(repo: MavenRepository, credentials: Option[DirectCredentials]) = {
    val r = new RemoteRepository(repo.name, "default", repo.root)
    credentials.foreach(c => {
      r.setAuthentication(new Authentication(c.userName, c.passwd))
    })
    r.setProxy(SystemPropertyProxySelector.getProxy(r))
    r
  }

  private def deployIt(artifact: AetherArtifact, wagons: Seq[WagonWrapper], repo: MavenRepository, credentials: Option[DirectCredentials])(implicit streams: TaskStreams) {
    val request = new DeployRequest()
    request.setRepository(toRepository(repo, credentials))
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))
    implicit val system = Booter.newRepositorySystem(wagons)
    implicit val localRepo = Path.userHome / ".m2" / "repository"

    try {
      system.deploy(Booter.newSession, request)
    }
    catch {
      case e: Exception => e.printStackTrace(); throw e
    }
  }

  private def installIt(artifact: AetherArtifact, wagons: Seq[WagonWrapper])(implicit streams: TaskStreams) {
    val request = new InstallRequest()
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))
    implicit val system = Booter.newRepositorySystem(wagons)
    implicit val localRepo = Path.userHome / ".m2" / "repository"

    try {
      system.install(Booter.newSession, request)
    }
    catch {
      case e: Exception => e.printStackTrace(); throw e
    }
  }
}

case class MavenCoordinates(groupId: String, artifactId: String, version: String, classifier: Option[String], extension: String = "jar") {
  def coordinates = "%s:%s:%s%s:%s".format(groupId, artifactId, extension, classifier.map(_ + ":").getOrElse(""), version)
}

object MavenCoordinates {
  def apply(coords: String): Option[MavenCoordinates] = coords.split(":") match {
    case Array(groupId, artifactId, extension, v) =>
      Some(MavenCoordinates(groupId, artifactId, v, None, extension))

    case Array(groupId, artifactId, extension, classifier, v) =>
      Some(MavenCoordinates(groupId, artifactId, v, Some(classifier), extension))

    case _ => None
  }
}

case class AetherSubArtifact(file: File, classifier: Option[String] = None, extension: String = "jar") {
  def toArtifact(parent: DefaultArtifact) = new SubArtifact(parent, classifier.orNull, extension, file)
}

case class AetherArtifact(file: File, coordinates: MavenCoordinates, subartifacts: Seq[AetherSubArtifact] = Nil) {
  def toArtifact = new DefaultArtifact(
    coordinates.groupId,
    coordinates.artifactId,
    coordinates.classifier.orNull,
    coordinates.extension,
    coordinates.version,
    Collections.emptyMap[String, String](),
    file
  )
}
