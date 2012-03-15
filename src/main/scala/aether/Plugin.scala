package aether

import sbt._
import sbt.Keys._
import java.util.Collections
import org.sonatype.aether.util.artifact.{SubArtifact, DefaultArtifact}
import org.sonatype.aether.deployment.DeployRequest
import org.sonatype.aether.repository.{Authentication, RemoteRepository}

object AetherKeys {
  lazy val attachedArtifacts = TaskKey[Seq[AetherSubArtifact]]("aether-attached-artifacts", "Attach these artifacts to the deployment, typically sources and scaladoc")
  lazy val aetherCredentials = SettingKey[Option[Credentials]]("aether-credentials", "Use these credentials when deploying")
  lazy val deployRepository = SettingKey[MavenRepository]("aether-deploy-repository", "Deploy to this repository")
  lazy val coordinates = SettingKey[MavenCoordinates]("aether-coordinates", "The maven coordinates to the main artifact. Should not be overridden")

}


object Aether extends sbt.Plugin {

  import AetherKeys._

  lazy val aetherSettings = inConfig(Compile)(baseAetherSettings)

  lazy val defaultAttaches = (packageSrc, packageDoc) map {(src: File, scaladoc: File) => Seq(AetherSubArtifact(src, Some("sources")), AetherSubArtifact(scaladoc, Some("javadoc")))}

  lazy val baseAetherSettings: Seq[Setting[_]] = Seq(
    attachedArtifacts <<= defaultAttaches,
    aetherCredentials := None,
    coordinates <<= (organization, name, version, scalaVersion).apply{(o, n, v, sv) => MavenCoordinates(o, n + "_" + sv, v, None)}
  ) ++ AetherDeploy.apply()
  
  class AetherDeploy(packageTaskKey: TaskKey[File]) {

    lazy val deploy = TaskKey[Unit]("aether-deploy", "Deploy artifact using Aether")

    lazy val printer = deploy <<= (organization) map ((o: String) => println(o))
    
    lazy val deployTask = deploy <<= (deployRepository, aetherCredentials, packageTaskKey in (Compile), makePom, coordinates, attachedArtifacts, streams).map{
      (repo: MavenRepository, cred: Option[Credentials], artifactFile: File, pom: File, c: MavenCoordinates, attached: Seq[AetherSubArtifact], s: TaskStreams) => {
      val artifact = AetherArtifact(artifactFile, c, List(AetherSubArtifact(pom, None, "pom")) ++ attached)
      deployIt(artifact, repo, cred)(s)
    }}

    
    def toRepository(repo: MavenRepository, credentials: Option[Credentials]) = {
      val r = new RemoteRepository(repo.name, "default", repo.root)
      if (credentials.isDefined) {
        val direct = Credentials.toDirect(credentials.get)
        r.setAuthentication(new Authentication(direct.userName, direct.passwd))
      }
      r
    }

    def deployIt(artifact: AetherArtifact, repo: MavenRepository, credentials: Option[Credentials])(implicit streams: TaskStreams) {
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

  object AetherDeploy {
    def apply(packageTaskKey: TaskKey[File]): Seq[Setting[_]] = {
      val deploy = new AetherDeploy(packageTaskKey)
      Seq(deploy.deployTask)
    }

    def apply(): Seq[Setting[_]] = apply(packageBin)
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

  def apply(groupId: String, version: String, artifact: Artifact) = {
    new MavenCoordinates(groupId, artifact.name, version, artifact.classifier, artifact.extension)
  }
}

case class AetherSubArtifact(file: File, classifier: Option[String] = None, extension: String = "jar") {
  def toArtifact(parent: DefaultArtifact) = new SubArtifact(parent, classifier.orNull, extension, file)
}

case class AetherArtifact(file: File, coordinates: MavenCoordinates, subartifacts: List[AetherSubArtifact] = Nil) {
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
