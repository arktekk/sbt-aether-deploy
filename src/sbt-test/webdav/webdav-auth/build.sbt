import aether.AetherKeys._

version := "0.1"

name := "webdav"

organization := "webdav"

scalaVersion := "2.9.1"

credentials += Credentials(file("./credentials"))

publishTo := Some("dav" at "dav://localhost:8008")

overridePublishSettings

aetherWagons := Seq(aether.WagonWrapper("dav", "org.apache.maven.wagon.providers.webdav.WebDavWagon"))
