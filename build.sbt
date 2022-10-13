import Dependencies._

ThisBuild / scalaVersion     := "2.13.8"
ThisBuild / version          := "0.1.0-SNAPSHOT"
ThisBuild / organization     := "com.example"
ThisBuild / organizationName := "example"


lazy val root = (project in file("."))
  .settings(
    name := "ethtxn",
    libraryDependencies ++= Seq(
      scalaTest % Test,
      "org.web3j" % "core" % "4.9.4",
      "org.web3j" % "crypto" % "4.9.4",
      "org.web3j" % "utils" % "4.9.4"
    )
  )

// See https://www.scala-sbt.org/1.x/docs/Using-Sonatype.html for instructions on how to publish to Sonatype.
