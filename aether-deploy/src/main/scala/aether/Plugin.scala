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

import sbtcompat.PluginCompat
import sbtcompat.PluginCompat._
import xsbti.FileConverter

import scala.util.{Failure, Success, Try}

object AetherKeys {
  val aetherArtifact          = taskKey[AetherArtifact]("Main artifact")
  val aetherCoordinates       = settingKey[MavenCoordinates]("Internal coordinates")
  val aetherDeploy            = TaskKey[Unit]("aether-deploy", "Deploys to a maven repository.")
  val aetherInstall           = TaskKey[Unit]("aether-install", "Installs to a local maven repository.")
  val aetherPackageMain       = taskKey[File]("package main Artifact")
  val aetherLocalRepo         = settingKey[File]("Local maven repository.")
  val aetherCustomHttpHeaders = settingKey[Map[String, String]]("Add these headers to the http request")

  /** Attach an additional sub-artefact (sourced from a task that produces a packaged file)
    * to the main `aetherArtifact`.
    */
  def attachSubArtifact(
      packageTask: sbt.TaskKey[sbtcompat.PluginCompat.FileRef],
      classifier: String,
      extension: String = "jar"
  ): Def.Setting[sbt.Task[AetherArtifact]] =
    aetherArtifact := Def.uncached {
      implicit val conv: xsbti.FileConverter = sbt.Keys.fileConverter.value
      aetherArtifact.value.attach(packageTask.value, classifier, extension)
    }
}

import AetherKeys._

object AetherPlugin extends AutoPlugin {
  override def trigger         = allRequirements
  override def requires        = sbt.plugins.IvyPlugin
  override def projectSettings = aetherBaseSettings ++ Seq(
    aetherArtifact := Def.uncached {
      implicit val conv: FileConverter = fileConverter.value
      createArtifact(
        (Compile / packagedArtifacts).value,
        aetherCoordinates.value,
        aetherPackageMain.value
      )
    }
  )

  object autoImport {
    def overridePublishSettings: Seq[Setting[?]]      = Seq(publish := aetherDeploy.value)
    def overridePublishLocalSettings: Seq[Setting[?]] =
      Seq(publishLocal := {
        publishLocal.value
        aetherInstall.value
      })
    def overridePublishBothSettings: Seq[Setting[?]]  = overridePublishSettings ++ overridePublishLocalSettings
  }

  lazy val aetherBaseSettings: Seq[Setting[?]] = Seq(
    aetherLocalRepo := Path.userHome / ".m2" / "repository",
    defaultCoordinates,
    deployTask,
    installTask,
    aetherPackageMain := Def.uncached {
      implicit val conv: FileConverter = fileConverter.value
      PluginCompat.toFile((Compile / Keys.`package`).value)
    },
    sbtPluginPublishLegacyMavenStyle := false,
    aetherDeploy / version := version.value,
    aetherDeploy / logLevel := Level.Debug,
    aetherCustomHttpHeaders := Map.empty[String, String]
  )

  def defaultCoordinates = aetherCoordinates := {
    val art        = artifact.value
    val theVersion = (aetherDeploy / version).value
    val sbtBin     = (pluginCrossBuild / sbtBinaryVersion).value
    val scalaBin   = scalaBinaryVersion.value

    val artifactId =
      if (sbtPlugin.value) pluginArtifactId(art.name, sbtBin, scalaBin)
      else
        CrossVersion(crossVersion.value, scalaVersion.value, scalaBin)
          .map(_(art.name))
          .getOrElse(art.name)

    val coords = MavenCoordinates(organization.value, artifactId, theVersion, None, art.extension)
    if (sbtPlugin.value) coords.sbtPlugin().withSbtVersion(sbtBin).withScalaVersion(scalaBin)
    else coords
  }

  private def pluginArtifactId(name: String, sbtBin: String, scalaBin: String): String = {
    val major: Option[Int] = sbtBin.split('.').headOption.flatMap { s =>
      scala.util.Try(s.toInt).toOption
    }
    major match {
      case Some(m) if m >= 2 => s"${name}_sbt${m}_${scalaBin}"
      case _                 => s"${name}_${scalaBin}_${sbtBin}"
    }
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
      artifacts: Map[Artifact, PluginCompat.FileRef],
      coords: MavenCoordinates,
      mainArtifact: File
  )(implicit conv: FileConverter): AetherArtifact = {
    val artifactsAsFiles: Map[Artifact, File] =
      artifacts.map { case (a, v) => a -> PluginCompat.toFile(v) }

    val prefiltered                           = artifactsAsFiles.filterNot { case (a, f) =>
      mainArtifact == f || (a.classifier.isEmpty && a.extension == "jar")
    }

    val subArtifacts = prefiltered.map { case (art, f) =>
      AetherSubArtifact(f, art.classifier, art.extension)
    }.toList

    val realCoords = coords.withExtension(mainArtifact)

    AetherArtifact(mainArtifact, realCoords, subArtifacts)
  }

  def deployIt(
      repo: Option[Resolver],
      localRepo: File,
      artifact: AetherArtifact,
      cred: Seq[Credentials],
      customHeaders: Map[String, String]
  )(stream: sbt.std.TaskStreams[?]): Unit = {
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
      val c    = PluginCompat.credentialForHost(cred, href.getHost)
      if (c.isEmpty && href.getHost != null) {
        stream.log.warn("No credentials supplied for %s".format(href.getHost))
      }
      c
    }.toOption.flatten

    val builder = new Builder(repository.name, "default", repository.url.toString)
    maybeCred.foreach { c =>
      builder.setAuthentication(new AuthenticationBuilder().addUsername(c.userName).addPassword(c.passwd).build())
    }

    val request = new DeployRequest()
    request.setRepository(builder.build())
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

  def installIt(artifact: AetherArtifact, localRepo: File)(streams: sbt.std.TaskStreams[?]): Unit = {

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
