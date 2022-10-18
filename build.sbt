import Dependencies._

ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "beautyShop",
    libraryDependencies ++= Seq(
      akkaHttp,
      akkaStream,
      specs2Core % Test,
      jsonData,
      jacksonData,
      mongoDb,
      log,
      scalaLogging
    )
  )
