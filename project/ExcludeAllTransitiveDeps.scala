import sbt._
import xml._
import java.io.File

object ExcludeAllTransitiveDeps {
  val root = "http://central.maven.org/maven2/"

  def apply(target: File, module: ModuleID): ModuleID = {
    val name = module.name
    val org = module.organization.replace('.', '/')
    val version = module.revision
    val pom = target / (name + "-pom.xml")
    if (!pom.exists) {
      val u = url(s"$root/$org/$name/$version/$name-$version.pom")
      IO.download(u, pom)
    }
    module.copy(exclusions = makeExclusions(pom))
  }

  def makeExclusions(pom: File): Vector[ExclusionRule] = {
    val root = XML.loadFile(pom)

    val dependencies = root \ "dependencies" \ "dependency"
    val res = dependencies.foldLeft(Vector.empty[ExclusionRule]){ case (m, e) =>
      val gid = (e \ "groupId").text
      val aid = (e \ "artifactId").text
      m :+ ExclusionRule(gid, aid)
    }
    res
  }
}
