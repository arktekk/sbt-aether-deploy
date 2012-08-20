version := "0.1"

name := "webdav"

organization := "webdav"

scalaVersion := "2.9.1"

publishTo := Some("dav" at "dav://localhost:8008")

seq(aetherPublishSettings: _*)

wagons := Seq(aether.WagonWrapper("dav", new org.apache.maven.wagon.providers.webdav.WebDavWagon()))
