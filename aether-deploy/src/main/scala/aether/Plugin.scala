package aether

import aether.internal._
import sbt._
import Keys.{version, _}
import org.eclipse.aether.deployment.DeployRequest
import org.eclipse.aether.installation.InstallRequest
import org.eclipse.aether.repository.RemoteRepository

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
  val aetherCustomHttpHeaders = settingKey[Map[String, String]]("Add these headers to the http request")
}

import AetherKeys._

object AetherPlugin extends AutoPlugin {
  override def trigger         = allRequirements
  override def requires        = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(
    aetherArtifact := {
      createArtifact(
        (Compile / packagedArtifacts).value,
        aetherCoordinates.value,
        aetherPackageMain.value
      )
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
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask,
    aetherPackageMain := {
      (Compile / Keys.`package`).value
    },
    sbtPluginPublishLegacyMavenStyle := false,
    aetherDeploy / version := (ThisBuild / version).value,
    aetherDeploy / logLevel := Level.Debug,
    aetherCustomHttpHeaders := Map.empty[String, String]
  )

  def defaultCoordinates = aetherCoordinates := {
    val art        = artifact.value
    val theVersion = (aetherDeploy / version).value

    val defaultArtifactId =
      CrossVersion(crossVersion.value, scalaVersion.value, scalaBinaryVersion.value).map(_(art.name)) getOrElse art.name

    val artifactId =
      if (sbtPlugin.value) "%s_%s".format(defaultArtifactId, (pluginCrossBuild / sbtBinaryVersion).value)
      else defaultArtifactId
    val coords     = MavenCoordinates(organization.value, artifactId, theVersion, None, art.extension)
    if (sbtPlugin.value)
      coords.sbtPlugin().withSbtVersion((pluginCrossBuild / sbtBinaryVersion).value).withScalaVersion(scalaBinaryVersion.value)
    else coords
  }

  lazy val deployTask = aetherDeploy := Def
    .taskDyn {
      val _skip     = (publish / skip).value
      val doPublish = (Compile / publishArtifact).value
      if (doPublish && !_skip) {
        Def.task {
          deployIt(
            publishTo.value,
            aetherLocalRepo.value,
            aetherArtifact.value,
            credentials.value,
            aetherCustomHttpHeaders.value
          )(streams.value)
        }
      } else {
        Def.task(())
      }
    }
    .tag(Tags.Publish, Tags.Network)
    .value

  lazy val installTask = aetherInstall := Def
    .task {
      installIt(aetherArtifact.value, aetherLocalRepo.value)(streams.value)
    }
    .tag(Tags.Publish, Tags.Network)
    .value

  def createArtifact(
      artifacts: Map[Artifact, sbt.File],
      coords: MavenCoordinates,
      mainArtifact: File
  ): AetherArtifact = {
    val prefiltered = artifacts.filterNot { case (a, f) =>
      mainArtifact == f || (a.classifier.isEmpty && a.extension == "jar")
    }

    val subArtifacts = prefiltered.map { case (art, f) =>
      AetherSubArtifact(f, art.classifier, art.extension)
    }.toList

    val realCoords = coords.withExtension(mainArtifact)

    AetherArtifact(mainArtifact, realCoords, subArtifacts)
  }

  private def toRepository(
      repo: RepoRef,
      credentials: Option[DirectCredentials]
  ): RemoteRepository = {
    val builder: Builder = new Builder(repo.name, "default", repo.url.toString)
    credentials.foreach { c =>
      println(c)
      builder.setAuthentication(new AuthenticationBuilder().addUsername(c.userName).addPassword(c.passwd).build())
    }
    builder.build()
  }

  def deployIt(
      repo: Option[Resolver],
      localRepo: File,
      artifact: AetherArtifact,
      cred: Seq[Credentials],
      customHeaders: Map[String, String]
  )(implicit
      stream: TaskStreams
  ) {
    object IsPatternMavenRepo {
      def unapply(resolver: Resolver): Option[(sbt.PatternsBasedRepository, String)] = {
        resolver match {
          case repository: FileRepository if repository.patterns.isMavenCompatible =>
            repository.patterns.artifactPatterns.headOption
              .flatMap { pattern =>
                val idx = pattern.indexOf(Resolver.mavenStyleBasePattern)
                if (idx > 0) Some(pattern.substring(0, idx - 1))
                else None
              }
              .map(repository -> _)
          case _                                                                   => None
        }
      }
    }

    val repository = repo
      .collect {
        case x: MavenRepository            => RepoRef(x.name, URI.create(x.root))
        case IsPatternMavenRepo((x, root)) => RepoRef(x.name, URI.create(if (root.startsWith("/")) "file:" + root else root))
        case x                             => sys.error("The configured repo MUST be a maven compatible repo, but was: " + x)
      }
      .getOrElse(sys.error("There MUST be a configured publish repo"))

    val maybeCred = Try {
      val href = repository.url
      val c    = Credentials.forHost(cred, href.getHost)
      if (c.isEmpty && href.getHost != null) {
        stream.log.warn("No credentials supplied for %s".format(href.getHost))
      }
      c
    }.toOption.flatten

    val request = new DeployRequest()
    request.setRepository(toRepository(repository, maybeCred))
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

  case class RepoRef(name: String, url: URI)
}
