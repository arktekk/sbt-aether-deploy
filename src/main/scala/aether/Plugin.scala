package aether

import sbt._
import Keys._
import org.eclipse.aether.util.artifact.SubArtifact
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.{Proxy, RemoteRepository}
import java.net.URI
import org.eclipse.aether.repository.RemoteRepository.Builder
import org.eclipse.aether.util.repository.AuthenticationBuilder
import com.typesafe.sbt.pgp.PgpKeys

object Aether extends sbt.Plugin {
  lazy val aetherArtifact = TaskKey[AetherArtifact]("Main artifact")
  lazy val coordinates = SettingKey[MavenCoordinates]("The maven coordinates to the main artifact. Should not be overridden")
  lazy val wagons = SettingKey[Seq[WagonWrapper]]("The configured extra maven wagon wrappers.")
  lazy val deploy = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  lazy val install = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")
  lazy val aetherLocalRepo = SettingKey[File]("Local maven repository.")

  lazy val aetherBaseSettings: Seq[Setting[_]] = Seq(
    wagons := Seq.empty,
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask
  )


  lazy val aetherSettings: Seq[Setting[_]] = aetherBaseSettings ++ Seq(defaultCoordinates)

  lazy val aetherPublishSettings: Seq[Setting[_]] = aetherSettings ++ Seq(publish <<= deploy)
  lazy val aetherPublishLocalSettings: Seq[Setting[_]] = aetherSettings ++ Seq(publishLocal <<= install.dependsOn(publishLocal))
  lazy val aetherPublishBothSettings: Seq[Setting[_]] = aetherSettings ++ Seq(publish <<= deploy, publishLocal <<= install.dependsOn(publishLocal))

  //Signed
  lazy val aetherSignedSettings: Seq[Setting[_]] = aetherBaseSettings ++ Seq(signedArtifact)

  lazy val aetherPublishSignedSettings: Seq[Setting[_]] = aetherSignedSettings ++ Seq(PgpKeys.publishSigned <<= deploy)
  lazy val aetherPublishSignedLocalSettings: Seq[Setting[_]] = aetherSignedSettings ++ Seq(PgpKeys.publishLocalSigned <<= install.dependsOn(PgpKeys.publishLocalSigned))
  lazy val aetherPublishSignedBothSettings: Seq[Setting[_]] = aetherSignedSettings ++ Seq(PgpKeys.publishSigned <<= deploy, PgpKeys.publishLocalSigned <<= install.dependsOn(PgpKeys.publishLocalSigned))


  lazy val defaultCoordinates = coordinates <<= (organization, artifact, version, sbtBinaryVersion, scalaBinaryVersion, crossPaths, sbtPlugin).apply{
     (o, artifact, v, sbtV, scalaV, crossPath, plugin) => {
      val artifactId = if (crossPath && !plugin) "%s_%s".format(artifact.name, scalaV) else artifact.name
      val coords = MavenCoordinates(o, artifactId, v, None, artifact.extension)
      if (plugin) coords.withSbtVersion(sbtV).withScalaVersion(scalaV) else coords
    }
  }

  def defaultArtifact = aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, packagedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
      createArtifact(artifacts, pom, coords, mainArtifact)
  }

  def signedArtifact = aetherArtifact <<= (coordinates, Keys.`package` in Compile, makePom in Compile, PgpKeys.signedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
      createArtifact(artifacts, pom, coords, mainArtifact)
  }

  lazy val deployTask = deploy <<= (publishTo, sbtPlugin, wagons, credentials, aetherArtifact, streams, aetherLocalRepo).map{
    (repo: Option[Resolver], plugin: Boolean, wag: Seq[WagonWrapper], cred: Seq[Credentials], artifact: AetherArtifact, s: TaskStreams, localR: File) => {
      deployIt(repo, localR, artifact, plugin, wag, cred)(s)
    }}

  lazy val installTask = install <<= (aetherArtifact, aetherLocalRepo, streams, sbtPlugin).map{
    (artifact: AetherArtifact, localR: File, s: TaskStreams, plugin: Boolean) => {
      installIt(artifact, localR, plugin)(s)
    }}

  def createArtifact(artifacts: Map[Artifact, sbt.File], pom: sbt.File, coords: MavenCoordinates, mainArtifact: File): AetherArtifact = {
    val filtered = artifacts.filterNot {
       case (a, f) => a.classifier == None && !a.extension.contains("asc")
    }
    val subArtifacts = AetherSubArtifact(pom, None, "pom") +: filtered.foldLeft(Vector[AetherSubArtifact]()) { case (seq, (a, f)) => AetherSubArtifact(f, a.classifier, a.extension) +: seq}
 
    AetherArtifact(mainArtifact, coords, subArtifacts)
  }
    
  private def toRepository(repo: MavenRepository, plugin: Boolean, credentials: Option[DirectCredentials]): RemoteRepository = {
    val builder: Builder = new Builder(repo.name, if (plugin) "sbt-plugin" else "default", repo.root)
    credentials.foreach{c => builder.setAuthentication(new AuthenticationBuilder().addUsername(c.userName).addPassword(c.passwd).build()) }
    val proxy: Option[Proxy] = Option(SystemPropertyProxySelector.selector.getProxy(builder.build()))
    proxy.foreach(p => builder.setProxy(p))
    builder.build()
  }

  def deployIt(repo: Option[Resolver], localRepo: File, artifact: AetherArtifact, plugin: Boolean, wagons: Seq[WagonWrapper], cred: Seq[Credentials])(implicit s: TaskStreams) {
    val repository = repo.collect{
      case x: MavenRepository => x
      case x => sys.error("The configured repo MUST be a maven repo, but was: " + x)
    }.getOrElse(sys.error("There MUST be a configured publish repo"))

    val maybeCred = scala.util.control.Exception.allCatch.apply {
      val href = URI.create(repository.root)
      val c = Credentials.forHost(cred, href.getHost)
      if (c.isEmpty) {
         s.log.warn("No credentials supplied for %s".format(href.getHost))
      }
      c
    }

    val request = new DeployRequest()
    request.setRepository(toRepository(repository, plugin, maybeCred))
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))

    try {
      val (system, session) = Booter(localRepo, s, wagons, plugin)
      system.deploy(session, request)
    }
    catch {
      case e: Exception => e.printStackTrace(); throw e
    }
  }

  def installIt(artifact: AetherArtifact, localRepo: File, plugin: Boolean)(implicit streams: TaskStreams) {

    val request = new InstallRequest()
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))

    try {
      val (system, session) = Booter(localRepo, streams, Nil, plugin)
      system.install(session, request)
    }
    catch {
      case e: Exception => e.printStackTrace(); throw e
    }
  }
}

case class MavenCoordinates(groupId: String, artifactId: String, version: String, classifier: Option[String], extension: String = "jar", props: Map[String, String] = Map.empty) {
  def coordinates = "%s:%s:%s%s:%s".format(groupId, artifactId, extension, classifier.map(_ + ":").getOrElse(""), version)

  def withScalaVersion(v: String) = withProp(MavenCoordinates.ScalaVersion, v)
  def withSbtVersion(v: String) = withProp(MavenCoordinates.SbtVersion, v)

  def withProp(name: String, value: String) = copy(props = props.updated(name, value))
}

object MavenCoordinates {
  val ScalaVersion = "scala-version"
  val SbtVersion = "sbt-version"

  def apply(coords: String): Option[MavenCoordinates] = coords.split(":") match {
    case Array(groupId, artifactId, extension, v) =>
      Some(MavenCoordinates(groupId, artifactId, v, None, extension))

    case Array(groupId, artifactId, extension, classifier, v) =>
      Some(MavenCoordinates(groupId, artifactId, v, Some(classifier), extension))

    case _ => None
  }
}

case class AetherSubArtifact(file: File, classifier: Option[String] = None, extension: String = "jar") {
  def toArtifact(parent: DefaultArtifact) = new SubArtifact(parent, classifier.orNull, extension, parent.getProperties, file)
}

case class AetherArtifact(file: File, coordinates: MavenCoordinates, subartifacts: Seq[AetherSubArtifact] = Nil) {
  def isSbtPlugin = coordinates.props.contains(MavenCoordinates.SbtVersion)

  def attach(file: File, classifier: String, extension: String = "jar") = {
    copy(subartifacts = subartifacts :+ AetherSubArtifact(file, Some(classifier), extension))
  }

  import collection.JavaConverters._
  def toArtifact = new DefaultArtifact(
    coordinates.groupId,
    coordinates.artifactId,
    coordinates.classifier.orNull,
    coordinates.extension,
    coordinates.version,
    coordinates.props.asJava,
    file
  )
}
