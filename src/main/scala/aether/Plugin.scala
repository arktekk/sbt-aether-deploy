package aether

import sbt._
import Keys.{version, _}
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.{Proxy, RemoteRepository}
import java.net.URI

import org.eclipse.aether.repository.RemoteRepository.Builder
import org.eclipse.aether.util.repository.AuthenticationBuilder
import internal._

object AetherKeys {
  val aetherArtifact = taskKey[AetherArtifact]("Main artifact")
  val aetherCoordinates = settingKey[MavenCoordinates]("Internal coordinates")
  val aetherDeploy = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  val aetherInstall = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")
  val aetherPackageMain = taskKey[File]("package main Artifact")
  val aetherLocalRepo = settingKey[File]("Local maven repository.")
  val aetherOldVersionMethod = settingKey[Boolean]("Flag for using the old method of getting the version")
}

import AetherKeys._

object AetherPlugin extends AetherPlugin {
  override def trigger = allRequirements
  override def requires = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(
    aetherArtifact := {
      createArtifact((packagedArtifacts in Compile).value, aetherCoordinates.value, aetherPackageMain.value)
    }
  )

  object autoImport {
    def overridePublishSettings: Seq[Setting[_]] = Seq(publish := aetherDeploy.value)
    def overridePublishLocalSettings: Seq[Setting[_]] = Seq(publishLocal := {
      publishLocal.value
      aetherInstall.value
    })
    def overridePublishBothSettings: Seq[Setting[_]] = overridePublishSettings ++ overridePublishLocalSettings
  }
}


trait AetherPlugin extends AutoPlugin {

  lazy val aetherBaseSettings: Seq[Setting[_]] = Seq(
    //aetherWagons := Seq.empty,
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask,
    aetherPackageMain := {
      (Keys.`package` in Compile).value
    },
    aetherOldVersionMethod := false,
    logLevel in aetherDeploy := Level.Debug
  )


  def defaultCoordinates = aetherCoordinates := {
    val art = artifact.value
    val theVersion = if (aetherOldVersionMethod.value) version.value else (version in ThisBuild).value

    val artifactId = if (!sbtPlugin.value) CrossVersion(crossVersion.value, scalaVersion.value, scalaBinaryVersion.value).map(_(art.name)) getOrElse art.name else art.name
    val coords = MavenCoordinates(organization.value, artifactId, theVersion, None, art.extension)
    if (sbtPlugin.value) coords.withSbtVersion((sbtBinaryVersion in pluginCrossBuild).value).withScalaVersion(scalaBinaryVersion.value) else coords
  }

  lazy val deployTask = aetherDeploy := Def.taskDyn{
    if ((publishArtifact in Compile).value) {
      Def.task {
        deployIt(publishTo.value, aetherLocalRepo.value, aetherArtifact.value, sbtPlugin.value, credentials.value)(streams.value)
      }
    } else {
      Def.task(())
    }
  }.value

  lazy val installTask = aetherInstall := {
    installIt(aetherArtifact.value, aetherLocalRepo.value, sbtPlugin.value)(streams.value)
  }

  def createArtifact(artifacts: Map[Artifact, sbt.File], coords: MavenCoordinates, mainArtifact: File): AetherArtifact = {
    val subArtifacts = artifacts
      .filterNot { case (a, f) => a.classifier.isEmpty && f == mainArtifact }
      .map { case (a, f) => AetherSubArtifact(f, a.classifier, a.extension) }
      .toSeq

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

  def deployIt(repo: Option[Resolver], localRepo: File, artifact: AetherArtifact, plugin: Boolean, cred: Seq[Credentials])(implicit strem: TaskStreams) {
    val repository = repo.collect{
      case x: MavenRepository => x
      case x => sys.error("The configured repo MUST be a maven repo, but was: " + x)
    }.getOrElse(sys.error("There MUST be a configured publish repo"))

    val maybeCred = scala.util.control.Exception.allCatch.apply {
      val href = URI.create(repository.root)
      val c = Credentials.forHost(cred, href.getHost)
      if (c.isEmpty && href.getHost != null) {
         strem.log.warn("No credentials supplied for %s".format(href.getHost))
      }
      c
    }

    val request = new DeployRequest()
    request.setRepository(toRepository(repository, plugin, maybeCred))
    val parent = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))

    try {
      val (system, session) = Booter(localRepo, strem, plugin)
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
      val (system, session) = Booter(localRepo, streams, plugin)
      system.install(session, request)
    }
    catch {
      case e: Exception => e.printStackTrace(); throw e
    }
  }
}
