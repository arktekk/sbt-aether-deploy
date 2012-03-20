package aether

import sbt._
import sbt.Keys._
import java.util.Collections
import org.sonatype.aether.util.artifact.{SubArtifact, DefaultArtifact}
import org.sonatype.aether.deployment.DeployRequest
import org.sonatype.aether.repository.{Authentication, RemoteRepository}

object AetherKeys {
  lazy val aetherArtifact = TaskKey[AetherArtifact]("aether-artifact", "Main artifact")
  lazy val aetherCredentials = SettingKey[Option[Credentials]]("aether-credentials", "Use these credentials when deploying")
  lazy val deployRepository = SettingKey[MavenRepository]("aether-deploy-repository", "Deploy to this repository")
  lazy val coordinates = SettingKey[MavenCoordinates]("aether-coordinates", "The maven coordinates to the main artifact. Should not be overridden")
  lazy val deploy = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
}


object Aether extends sbt.Plugin {

  def deployRepository = AetherKeys.deployRepository in Global
  def aetherCredentials = AetherKeys.aetherCredentials in Global
  def deploy = AetherKeys.deploy in Global

  lazy val aetherSettings: Seq[Setting[_]] = Seq(
    AetherKeys.aetherCredentials := None,
    defaultCoordinates,
    defaultArtifact,
    deployTask
  )

  lazy val defaultCoordinates = AetherKeys.coordinates <<= (organization, name, version, scalaVersion).apply{(o, n, v, sv) => MavenCoordinates(o, n + "_" + sv, v, None)}
  
  lazy val defaultArtifact = AetherKeys.aetherArtifact <<= (AetherKeys.coordinates, Keys.`package` in Compile, makePom in Compile, packagedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) => {
      val subartifacts = artifacts.filterNot{case (a, f) => a.classifier == None && !a.extension.contains("asc")}
      val actualSubArtifacts = AetherSubArtifact(pom, None, "pom") +: subartifacts.foldLeft(Vector[AetherSubArtifact]()){case (seq, (a, f)) => AetherSubArtifact(f, a.classifier, a.extension) +: seq}
      val actualCoords = coords.copy(extension = getActualExtension(mainArtifact))
      AetherArtifact(mainArtifact, actualCoords, actualSubArtifacts)
    }
  }

  lazy val deployTask = AetherKeys.deploy <<= (AetherKeys.deployRepository, AetherKeys.aetherCredentials, AetherKeys.aetherArtifact, streams).map{
    (repo: MavenRepository, cred: Option[Credentials], artifact: AetherArtifact, s: TaskStreams) => {
      deployIt(artifact, repo, cred)(s)
    }}

  private def getActualExtension(file: File) = {
    val name = file.getName
    name.substring(name.lastIndexOf('.') + 1)
  }
    
  private def toRepository(repo: MavenRepository, credentials: Option[Credentials]) = {
    val r = new RemoteRepository(repo.name, "default", repo.root)
    if (credentials.isDefined) {
      val direct = Credentials.toDirect(credentials.get)
      r.setAuthentication(new Authentication(direct.userName, direct.passwd))
    }
    r
  }

  private def deployIt(artifact: AetherArtifact, repo: MavenRepository, credentials: Option[Credentials])(implicit streams: TaskStreams) {
    val request = new DeployRequest()
    request.setRepository(toRepository(repo, credentials))
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))
    implicit val system = Booter.newRepositorySystem
    implicit val localRepo = Path.userHome / ".m2" / "repository"

    try {
      system.deploy(Booter.newSession, request)
    }
    catch {
      case e => e.printStackTrace(); throw e
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
