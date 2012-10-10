import sbt._

import Keys._
import AndroidKeys._

object General {
  val settings = Defaults.defaultSettings ++ Seq (
    name := "ApplicationTestByScala",
    version := "0.1",
    versionCode := 0,
    scalaVersion := "2.9.2",
    platformName in Android := "android-10"
  )

  val proguardSettings = Seq (
    useProguard in Android := true
  )

  lazy val fullAndroidSettings =
    General.settings ++
    AndroidProject.androidSettings ++
    TypedResources.settings ++
    proguardSettings ++
    AndroidManifestGenerator.settings ++
    AndroidMarketPublish.settings ++ Seq (
      keyalias in Android := "change-me",
      libraryDependencies += "org.scalatest" %% "scalatest" % "1.8" % "test"
    )
}

object GenerateShrinkedScalaLibraryJar {
  def copyClassDirectoriesTask(main: Project, test: Project): Project.Initialize[Task[Unit]] = (classDirectory in Compile, classDirectory in Compile in main, classDirectory in Compile in test) map { (dst, src0, src1) =>
    IO.delete(dst)
    IO.copyDirectory(src0, dst)
    IO.copyDirectory(src1, dst)
  }

  def filterJar(minjar: File): File = {
    def retrievePackage(rootDir: File, file: File): String = {
      file.getAbsolutePath.stripPrefix(rootDir.getAbsolutePath).stripPrefix(java.io.File.separator)
    }

    val tmpDir = new File(minjar.getParent, "tmp")
    val filter = new SimpleFilter(_.startsWith("scala")) | new ExactFilter("library.properties")
    val files = IO.unzip(minjar, tmpDir, filter)
    val output = new File(minjar.getParent, "scala-library.shrinked.jar")
    val sources = files.map { file => file -> retrievePackage(tmpDir, file) }
    IO.zip(sources, output)
    IO.delete(tmpDir)
    output
  }

  val copyClassDirectories = TaskKey[Unit]("copy-class-directories")
  val generateShrinkedScalaLibraryJar = TaskKey[Option[File]]("generate-shrinked-scala-library-jar")

  def settings(main: Project, test: Project) = Seq (
    proguardOption in Android += """
    | -keep class scala.ScalaObject
    | -keepclasseswithmembers class scala.reflect.ScalaSignature { <methods>; }
    """.stripMargin,
    copyClassDirectories <<= copyClassDirectoriesTask(main, test),
    generateShrinkedScalaLibraryJar <<= (proguard in Android) map { minjar =>
      minjar.map(filterJar(_))
    } dependsOn(copyClassDirectories)
  )

  def mainProjectSettings(gen: Project) = Seq (
    proguardOption in Android += "-keep class scala.** { <fields>; <methods>; }",
    proguardInJars in Android <<= (proguardInJars in Android, scalaInstance, generateShrinkedScalaLibraryJar in gen) map { (jars, scalaInstance, shrinked) =>
      jars.filterNot(_ == scalaInstance.libraryJar) :+ shrinked.getOrElse(scalaInstance.libraryJar)
    }
  )

  lazy val testProjectSettings = Seq (
    proguardInJars in Android <<= (proguardInJars in Android, scalaInstance) map { (jars, scalaInstance) =>
      jars.filterNot(_ == scalaInstance.libraryJar)
    }
  )
}

object AndroidBuild extends Build {
  lazy val main = Project (
    "ApplicationTestByScala",
    file("."),
    settings = General.fullAndroidSettings ++
               GenerateShrinkedScalaLibraryJar.mainProjectSettings(generateScalaLibraryJar)
  )

  lazy val tests = Project (
    "tests",
    file("tests"),
    settings = General.settings ++
               AndroidTest.androidSettings ++
               General.proguardSettings ++
               GenerateShrinkedScalaLibraryJar.testProjectSettings ++ Seq (
      name := "ApplicationTestByScalaTests"
    )
  ) dependsOn main

  lazy val generateScalaLibraryJar: Project = Project (
    "generate-scala-library-jar",
    file("generate-scala-library-jar"),
    settings = General.settings ++
               AndroidProject.androidSettings ++
               General.proguardSettings ++
               GenerateShrinkedScalaLibraryJar.settings(main, tests)
  )
}
