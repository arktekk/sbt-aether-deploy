package aether

import aether.internal._
import sbt._
import Keys.{version, _}
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.{Proxy, RemoteRepository}
import java.net.URI

import org.eclipse.aether.repository.RemoteRepository.Builder
import org.eclipse.aether.util.repository.AuthenticationBuilder

import scala.util.{Failure, Success, Try}

object AetherKeys {
  val aetherArtifact          = taskKey[AetherArtifact]("Main artifact")
  val aetherCoordinates       = settingKey[MavenCoordinates]("Internal coordinates")
  val aetherDeploy            = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  val aetherInstall           = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")
  val aetherPackageMain       = taskKey[File]("package main Artifact")
  val aetherLocalRepo         = settingKey[File]("Local maven repository.")
  val aetherOldVersionMethod  = settingKey[Boolean]("Flag for using the old method of getting the version")
  val aetherCustomHttpHeaders = settingKey[Map[String, String]]("Add these headers to the http request")
}

import AetherKeys._

object AetherPlugin extends AutoPlugin {
  override def trigger         = allRequirements
  override def requires        = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(
    aetherArtifact := {
      createArtifact((Compile / packagedArtifacts).value, aetherCoordinates.value, aetherPackageMain.value)
    }
  )

  object autoImport {
    def overridePublishSettings: Seq[Setting[_]]      = Seq(publish := aetherDeploy.value)
    def overridePublishLocalSettings: Seq[Setting[_]] =
      Seq(publishLocal := {
        publishLocal.value
        aetherInstall.value
      })
    def overridePublishBothSettings: Seq[Setting[_]]  = overridePublishSettings ++ overridePublishLocalSettings
  }

  lazy val aetherBaseSettings: Seq[Setting[_]] = Seq(
    //aetherWagons := Seq.empty,
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask,
    aetherPackageMain := {
      (Compile / Keys.`package`).value
    },
    aetherOldVersionMethod := false,
    aetherDeploy / version := { if (aetherOldVersionMethod.value) version.value else (ThisBuild / version).value },
    aetherDeploy / logLevel := Level.Debug,
    aetherCustomHttpHeaders := Map.empty[String, String]
  )

  def defaultCoordinates = aetherCoordinates := {
    val art        = artifact.value
    val theVersion = (aetherDeploy / version).value

    val artifactId =
      if (!sbtPlugin.value)
        CrossVersion(crossVersion.value, scalaVersion.value, scalaBinaryVersion.value).map(_(art.name)) getOrElse art.name
      else art.name
    val coords     = MavenCoordinates(organization.value, artifactId, theVersion, None, art.extension)
    if (sbtPlugin.value)
      coords.sbtPlugin().withSbtVersion((pluginCrossBuild / sbtBinaryVersion).value).withScalaVersion(scalaBinaryVersion.value)
    else coords
  }

  lazy val deployTask = aetherDeploy := Def.taskDyn {
    val _skip     = (publish / skip).value
    val doPublish = (Compile / publishArtifact).value
    if (doPublish && !_skip) {
      Def.task {
        deployIt(
          publishTo.value,
          aetherLocalRepo.value,
          aetherArtifact.value,
          sbtPlugin.value,
          credentials.value,
          aetherCustomHttpHeaders.value
        )(streams.value)
      }
    } else {
      Def.task(())
    }
  }.tag(Tags.Publish, Tags.Network).value

  lazy val installTask = aetherInstall := Def.task {
    installIt(aetherArtifact.value, aetherLocalRepo.value)(streams.value)
  }.tag(Tags.Publish, Tags.Network).value

  def createArtifact(artifacts: Map[Artifact, sbt.File], coords: MavenCoordinates, mainArtifact: File): AetherArtifact = {
    val subArtifacts = artifacts
      .filterNot { case (a, f) => a.classifier.isEmpty && f == mainArtifact }
      .map { case (a, f) => AetherSubArtifact(f, a.classifier, a.extension) }
      .toSeq

    val realCoords = coords.withExtension(mainArtifact)

    AetherArtifact(mainArtifact, realCoords, subArtifacts)
  }

  private def toRepository(repo: MavenRepository, plugin: Boolean, credentials: Option[DirectCredentials]): RemoteRepository = {
    val builder: Builder     = new Builder(repo.name, if (plugin) "sbt-plugin" else "default", repo.root)
    credentials.foreach { c =>
      builder.setAuthentication(new AuthenticationBuilder().addUsername(c.userName).addPassword(c.passwd).build())
    }
    val proxy: Option[Proxy] = Option(SystemPropertyProxySelector.selector.getProxy(builder.build()))
    proxy.foreach(p => builder.setProxy(p))
    builder.build()
  }

  def deployIt(
      repo: Option[Resolver],
      localRepo: File,
      artifact: AetherArtifact,
      plugin: Boolean,
      cred: Seq[Credentials],
      customHeaders: Map[String, String]
  )(implicit
      stream: TaskStreams
  ) {
    val repository = repo
      .collect {
        case x: MavenRepository => x
        case x                  => sys.error("The configured repo MUST be a maven repo, but was: " + x)
      }
      .getOrElse(sys.error("There MUST be a configured publish repo"))

    val maybeCred = Try {
      val href = URI.create(repository.root)
      val c    = Credentials.forHost(cred, href.getHost)
      if (c.isEmpty && href.getHost != null) {
        stream.log.warn("No credentials supplied for %s".format(href.getHost))
      }
      c
    }.toOption.flatten

    val request = new DeployRequest()
    request.setRepository(toRepository(repository, plugin, maybeCred))
    val parent  = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))

    Booter.deploy(localRepo, stream, artifact.coordinates, customHeaders, request) match {
      case Success(_)  => ()
      case Failure(ex) =>
        ex.printStackTrace()
        throw ex
    }
  }

  def installIt(artifact: AetherArtifact, localRepo: File)(implicit streams: TaskStreams) {

    val request = new InstallRequest()
    val parent  = artifact.toArtifact
    request.addArtifact(parent)
    artifact.subartifacts.foreach(s => request.addArtifact(s.toArtifact(parent)))

    Booter.install(localRepo, streams, artifact.coordinates, request) match {
      case Success(_)  => ()
      case Failure(ex) =>
        ex.printStackTrace()
        throw ex
    }
  }
}
