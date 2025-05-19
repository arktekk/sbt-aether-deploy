package aether

import java.io.File
import org.eclipse.aether.util.artifact.SubArtifact
import org.eclipse.aether.artifact.DefaultArtifact

case class MavenCoordinates(
    groupId: String,
    artifactId: String,
    version: String,
    classifier: Option[String],
    extension: String = "jar",
    props: Map[String, String] = Map.empty
) {
  def coordinates = "%s:%s:%s%s:%s".format(groupId, artifactId, extension, classifier.map(_ + ":").getOrElse(""), version)

  def sbtPlugin()                 = withProp(MavenCoordinates.SbtPlugin, "true")
  def withScalaVersion(v: String) = withProp(MavenCoordinates.ScalaVersion, v)
  def withSbtVersion(v: String)   = withProp(MavenCoordinates.SbtVersion, v)
  def withExtension(file: File) = {
    val ext = {
      val i = file.getName.lastIndexOf(".")
      file.getName.substring(i + 1)
    }.toLowerCase
    if (ext == extension) this else copy(extension = ext)
  }

  def withProp(name: String, value: String) = copy(props = props.updated(name, value))
}

object MavenCoordinates {
  val SbtPlugin    = "sbt-plugin"
  val ScalaVersion = "scala-version"
  val SbtVersion   = "sbt-version"

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
