import sbtassembly.Plugin.AssemblyKeys

import sbt._

import Keys._

import sbtassembly.Plugin._

import AssemblyKeys._

assemblySettings

name := "ISAPURL"

version := "1.0"

scalaVersion := "2.10.4"

libraryDependencies ++= Seq(
  "org.json4s" %% "json4s-native" % "3.2.10",
  "org.json4s" %% "json4s-jackson" % "3.2.10",
  "org.mongodb" % "casbah_2.10" % "2.7.3",
  "com.novus" %% "salat" % "1.9.9",
  "mysql" % "mysql-connector-java" % "5.1.33",
  "org.scalaj" %% "scalaj-http" % "0.3.16",
  "org.apache.poi" % "poi" % "3.11"
)

resolvers ++= Seq(
  "Akka Repository" at "http://repo.akka.io/releases/",
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
)

mainClass in assembly := Some("com.iflytek.url.processUrl")

assemblyOption in packageDependency ~= {
  _.copy(appendContentHash = true)
}

excludedJars in assembly <<= (fullClasspath in assembly) map { cp =>
  cp filter { x => x.data.getName.matches("sbt.*") ||
    x.data.getName.matches(".*macros.*") ||
    x.data.getName.matches(".*javax.servlet.*") ||
    x.data.getName.matches(".*javax.transaction.*") ||
    x.data.getName.matches(".*javax.mail.glassfish.*") ||
    x.data.getName.matches(".*javax.activation.*")||
    x.data.getName.matches(".*kryo-2.21.*")||
    x.data.getName.matches(".*minlog.*")||
    x.data.getName.matches(".*commons-beanutils.*")||
    x.data.getName.matches(".*commons-logging.*")||
    x.data.getName.matches(".*jcl-over-slf4j.*")||
    x.data.getName.matches(".*slf4j-api-1.7.2.*")||
    x.data.getName.matches(".*slf4j-api-1.7.5.*")
  }
}
    