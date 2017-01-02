//

lazy val V = new {
  lazy val cats                = "0.8.1"
  lazy val typesafeConfig      = "1.3.0"
  lazy val scalacheck          = "1.13.4"
  lazy val scalacheckShapeless = "1.1.4"
  lazy val shapeless           = "2.3.2"
  lazy val shocon              = "0.1.7"
}

lazy val root = (project in file("."))
  .aggregate(coreJVM, coreJS)
  .aggregate(genericJVM, genericJS)
  .aggregate(typesafeJVM)
  .aggregate(shoconJS, shoconJVM)
  .aggregate(catsJS, catsJVM)
  .aggregate(testsJS, testsJVM)
  .aggregate(docsJVM)


lazy val core        = module("core")
  .settings(yax.scala(file("modules/core_compat")))
lazy val coreJS      = core.js
lazy val coreJVM     = core.jvm

lazy val generic     = module("generic")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "com.chuusai"  %%% "shapeless" % V.shapeless
  ))
lazy val genericJS   = generic.js
lazy val genericJVM  = generic.jvm


lazy val typesafeJVM = module("config-typesafe")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe"  % "config"    % V.typesafeConfig
  ))
  .settings(yax(file("modules/config"), "typesafe"))
  .jvm


lazy val shocon      = module("config-shocon")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "eu.unicredit" %%% "shocon" % V.shocon
  ))
  .settings(yax(file("modules/config"), "shocon"))

lazy val shoconJS    = shocon.js
lazy val shoconJVM   = shocon.jvm


lazy val cats        = module("cats")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel"  %%% "cats" % V.cats
  ))
lazy val catsJS      = cats.js
lazy val catsJVM     = cats.jvm


lazy val tests       = module("tests", sourceConfig = Test, crossJS = true)
  .dependsOn(core)
  .dependsOn(generic)
  .settings(libraryDependencies ++= Seq(
    "org.scalacheck"             %%% "scalacheck"    % V.scalacheck,
    "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % V.scalacheckShapeless
  ).map(_ % "test"))
  .jvmConfigure(_.dependsOn(typesafeJVM))
  .jvmSettings(libraryDependencies ++= Seq(
    "com.typesafe"                % "config"        % V.typesafeConfig
  ))
lazy val testsJS     = tests.js
lazy val testsJVM    = tests.jvm


lazy val docsJVM     = module("docs")
  .settings(noPublishSettings)
  .settings(tutSettings)
  .jvm

//

import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }

def module(
  modName: String,
  sourceConfig: Configuration = Compile,
  crossJS: Boolean = false
): CrossProject = {

  val project0 = CrossProject(modName, file(s"modules/.$modName"), CrossType.Dummy)
    .settings(name := s"case-classy-$modName")
    .settings(scalaSource in sourceConfig := baseDirectory.value.getParentFile.getParentFile / modName)
    .settings(unmanagedSourceDirectories in Compile      := Nil)
    .settings(unmanagedSourceDirectories in Test         := Nil)
    .settings(unmanagedSourceDirectories in sourceConfig := Seq((scalaSource in sourceConfig).value))

  def crossPaths(paths: SettingKey[Seq[File]], suffix: String): Setting[Seq[File]] =
    paths ++= paths.value.map(path => new File(path.getPath + suffix))

  val project1 =
    if (crossJS)
      project0
        .jsSettings(crossPaths(unmanagedSourceDirectories in sourceConfig, "JS"))
        .jvmSettings(crossPaths(unmanagedSourceDirectories in sourceConfig, "JVM"))
    else project0

  project1
}
