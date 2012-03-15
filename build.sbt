sbtPlugin := true

name := "aether-deploy"

organization := "no.arktekk.sbt"

version := "0.1-SNAPSHOT"

description := "sbt plugin deploy correctly to a maven repository"

licenses := Seq("Apache 2 License" -> url("http://www.apache.org/licenses/LICENSE-2.0.txt"))

scalacOptions := Seq("-deprecation", "-unchecked")

publishArtifact in (Compile, packageBin) := true

publishArtifact in (Test, packageBin) := false

publishArtifact in (Compile, packageDoc) := false

publishArtifact in (Compile, packageSrc) := false


libraryDependencies ++= {
	val wagonVersion = "2.2"
	val mavenVersion = "3.0.4"
	Seq(
	"org.apache.maven" % "maven-aether-provider" % mavenVersion,
	"org.sonatype.aether" % "aether-connector-wagon" % "1.13.1",
	"org.sonatype.aether" % "aether-connector-file" % "1.13.1",
	"org.apache.maven.wagon" % "wagon-provider-api" % wagonVersion,
	"org.sonatype.maven" % "wagon-ahc" % "1.2.1",
	"org.codehaus.plexus" % "plexus-utils" % "2.1",
	"org.apache.maven.wagon" % "wagon-ftp" % wagonVersion,
	"org.apache.maven.wagon" % "wagon-ssh" % wagonVersion
)}

seq(ScriptedPlugin.scriptedSettings: _*)