ThisBuild / version := "0.1.0-SNAPSHOT"

ThisBuild / scalaVersion := "2.13.10"

lazy val root = (project in file("."))
  .settings(
    name := "LSA"
  )

libraryDependencies += "org.scala-lang" % "scala-library" % "2.13.6"
libraryDependencies += "org.apache.spark" %% "spark-core" % "3.2.1"

