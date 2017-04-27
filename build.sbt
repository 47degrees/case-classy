import sbtunidoc.BaseUnidocPlugin.autoImport._
import sbtunidoc.ScalaUnidocPlugin.autoImport._

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(genericJVM, genericJS)
  .aggregate(typesafeJVM)
  .aggregate(shoconJS, shoconJVM)
  .aggregate(catsJVM, catsJS)
  .aggregate(allTests)
  .aggregate(docs)

lazy val core =
  module("core")
    .settings(unmanagedSourceDirectories in Compile := Nil)
    .settings(unmanagedSourceDirectories in Test := Nil)
    .settings(yax(file("modules/core/src/main/scala"), Compile, Nil,
      yaxScala = true, yaxPlatform = true))

lazy val coreJS = core.js
lazy val coreJVM = core.jvm

lazy val generic =
  module("generic")
    .dependsOn(core)
    .crossDepSettings(Seq(%%("shapeless")): _*)
    .settings(
      libraryDependencies += compilerPlugin("com.github.wheaties" %% "twotails" % V.twoTails cross CrossVersion.full),
      ivyConfigurations += config("compile-only").hide,
      libraryDependencies += "com.github.wheaties" %% "twotails-annotations" % V.twoTails % "compile-only" cross CrossVersion.full,
      unmanagedClasspath in Compile ++= update.value.select(configurationFilter("compile-only"))
    )

lazy val genericJS = generic.js
lazy val genericJVM = generic.jvm

lazy val typesafeJVM =
  module("config-typesafe", hideFolder = true)
    .dependsOn(core)
    .settings(libraryDependencies += %("config", V.typesafeConfig))
    .settings(yax(file("modules/config"), Compile, "typesafe" :: Nil))
    .jvm

lazy val shocon =
  module("config-shocon", hideFolder = true)
    .dependsOn(core)
    .crossDepSettings(Seq(%%("cats"), %%("shocon")): _*)
    .settings(libraryDependencies ++= Seq(%("scala-reflect", scalaVersion.value) % "provided"))
    .settings(yax(file("modules/config"), Compile, "shocon" :: Nil))

lazy val shoconJS = shocon.js
lazy val shoconJVM = shocon.jvm

lazy val cats =
  module("cats")
    .dependsOn(core)
    .crossDepSettings(Seq(%%("cats")): _*)

lazy val catsJS = cats.js
lazy val catsJVM = cats.jvm

lazy val allTests = (project in file(".all-tests"))
  .settings(noPublishSettings)
  .aggregate(testsJS, testsJVM)
  .aggregate(testsTypesafeJVM)
  .aggregate(testsShoconJS, testsShoconJVM)

lazy val testing =
  module("testing")
    .settings(noPublishSettings)
    .crossDepSettings(testsSettings: _*)
    .settings(coverageExcludedPackages := "classy\\.testing\\..*")
    .dependsOn(core, generic, cats)

lazy val testingJS = testing.js
lazy val testingJVM = testing.jvm

lazy val tests =
  module("tests")
    .settings(noPublishSettings)
    .crossDepSettings(testsSettings.map(_ % "test"): _*)
    .dependsOn(testing)
    .dependsOn(core, generic, cats)

lazy val testsJS = tests.js
lazy val testsJVM = tests.jvm

lazy val testsTypesafeJVM =
  module("tests-config-typesafe", hideFolder = true)
    .jvm
    .settings(noPublishSettings)
    .settings(libraryDependencies ++= testsSettings.map(_ % "test"): _*)
    .dependsOn(testingJVM)
    .dependsOn(coreJVM, genericJVM)
    .dependsOn(typesafeJVM)
    .settings(yax(file("modules/tests-config"), Test, "typesafe" :: Nil))

lazy val testsShocon =
  module("tests-config-shocon", hideFolder = true)
    .settings(noPublishSettings)
    .crossDepSettings(testsSettings.map(_ % "test"): _*)
    .dependsOn(testing)
    .dependsOn(core, generic)
    .dependsOn(shocon)
    .settings(yax(file("modules/tests-config"), Test, "shocon" :: Nil))

lazy val testsShoconJS = testsShocon.js
lazy val testsShoconJVM = testsShocon.jvm

lazy val docs: Project =
  jvmModule("docs")
    .enablePlugins(MicrositesPlugin)
    .enablePlugins(ScalaUnidocPlugin)
    .dependsOn(coreJVM)
    .dependsOn(catsJVM)
    .dependsOn(genericJVM)
    .dependsOn(typesafeJVM)
    .settings(noPublishSettings)
    .settings(
      unidocProjectFilter in(ScalaUnidoc, unidoc) := inProjects(coreJVM, genericJVM, catsJVM, typesafeJVM, docs))
    .settings(docsSettings)

lazy val readme =
  jvmModule("readme")
    .dependsOn(coreJVM)
    .dependsOn(catsJVM)
    .dependsOn(genericJVM)
    .dependsOn(typesafeJVM)
    .settings(noPublishSettings)
    .settings(readmeSettings)
