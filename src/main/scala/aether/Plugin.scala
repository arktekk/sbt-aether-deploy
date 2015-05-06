package aether

import sbt._
import Keys._
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.{Proxy, RemoteRepository}
import java.net.URI
import org.eclipse.aether.repository.RemoteRepository.Builder
import org.eclipse.aether.util.repository.AuthenticationBuilder

import internal._

object AetherKeys {
  val aetherArtifact = TaskKey[AetherArtifact]("Main artifact")
  val aetherCoordinates = SettingKey[MavenCoordinates]("Internal coordinates")
  val aetherDeploy = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  val aetherInstall = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")
  val aetherPackageMain = TaskKey[File]("package main Artifact")
  val aetherWagons = SettingKey[Seq[WagonWrapper]]("The configured extra maven wagon wrappers.")
  val aetherLocalRepo = SettingKey[File]("Local maven repository.")
}

import AetherKeys._

object AetherPlugin extends AetherPlugin {
  override def trigger = allRequirements
  override def requires = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(defaultArtifact)

  object autoImport {
    def overridePublishSettings: Seq[Setting[_]] = Seq(publish <<= aetherDeploy)
    def overridePublishLocalSettings: Seq[Setting[_]] = Seq(publishLocal <<= aetherInstall.dependsOn(publishLocal))
    def overridePublishBothSettings: Seq[Setting[_]] = overridePublishSettings ++ overridePublishLocalSettings
  }
}


trait AetherPlugin extends AutoPlugin {
  
  lazy val aetherBaseSettings: Seq[Setting[_]] = Seq(
    aetherWagons := Seq.empty,
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask,
    aetherPackageMain <<= Keys.`package` in Compile
  )
 

  def defaultCoordinates = aetherCoordinates <<= (organization, artifact, version, sbtBinaryVersion, scalaBinaryVersion, crossPaths, sbtPlugin).apply{
     (o, artifact, v, sbtV, scalaV, crossPath, plugin) => {
      val artifactId = if (crossPath && !plugin) "%s_%s".format(artifact.name, scalaV) else artifact.name
      val coords = MavenCoordinates(o, artifactId, v, None, artifact.extension)
      if (plugin) coords.withSbtVersion(sbtV).withScalaVersion(scalaV) else coords
    }
  }

  def defaultArtifact = aetherArtifact <<= (aetherCoordinates, aetherPackageMain, makePom in Compile, packagedArtifacts in Compile) map {
    (coords: MavenCoordinates, mainArtifact: File, pom: File, artifacts: Map[Artifact, File]) =>
      createArtifact(artifacts, pom, coords, mainArtifact)
  }


  lazy val deployTask = aetherDeploy <<= (publishTo, sbtPlugin, aetherWagons, credentials, aetherArtifact, streams, aetherLocalRepo).map{
    (repo: Option[Resolver], plugin: Boolean, wag: Seq[WagonWrapper], cred: Seq[Credentials], artifact: AetherArtifact, s: TaskStreams, localR: File) => {
      deployIt(repo, localR, artifact, plugin, wag, cred)(s)
    }}

  lazy val installTask = aetherInstall <<= (aetherArtifact, aetherLocalRepo, streams, sbtPlugin).map{
    (artifact: AetherArtifact, localR: File, s: TaskStreams, plugin: Boolean) => {
      installIt(artifact, localR, plugin)(s)
    }}

  def createArtifact(artifacts: Map[Artifact, sbt.File], pom: sbt.File, coords: MavenCoordinates, mainArtifact: File): AetherArtifact = {
    val filtered = artifacts.filterNot {
       case (a, f) => a.classifier == None && !a.extension.contains("asc")
    }
    val subArtifacts = AetherSubArtifact(pom, None, "pom") +: filtered.foldLeft(Vector[AetherSubArtifact]()) { case (seq, (a, f)) => AetherSubArtifact(f, a.classifier, a.extension) +: seq}
 
    val realCoords = coords.withExtension(mainArtifact)

    AetherArtifact(mainArtifact, realCoords, subArtifacts)
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
      if (c.isEmpty && href.getHost != null) {
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
