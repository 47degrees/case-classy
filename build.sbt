import UnidocKeys._
//

lazy val V = new {
  lazy val cats                = "0.9.0"
  lazy val typesafeConfig      = "1.3.0"
  lazy val scalacheck          = "1.13.4"
  lazy val scalacheckShapeless = "1.1.4"
  lazy val shapeless           = "2.3.2"
  lazy val shocon              = "0.1.7"
  lazy val twoTails            = "0.3.1"
}

lazy val root = (project in file("."))
  .settings(noPublishSettings)
  .aggregate(coreJVM, coreJS)
  .aggregate(genericJVM, genericJS)
  .aggregate(typesafeJVM)
  .aggregate(shoconJS, shoconJVM)
  .aggregate(catsJVM, catsJS)
  .aggregate(allTests)
  .aggregate(docs)
  .settings(TaskKey[Unit]("copyReadme") := {
    (tutTargetDirectory in readme).value.listFiles().foreach(file =>
      IO.copyFile(file, new File((baseDirectory in ThisBuild).value, file.name)))
  })
  .settings(TaskKey[Unit]("checkDiff") := {
    val diff = "git diff".!!
    if (diff.nonEmpty)
      sys.error("Working directory is dirty!\n" + diff)
  })

addCommandAlias("validate", ";validateJS;validateJVM")
addCommandAlias("validateJVM", ";" + List(
  "coreJVM/compile",
  "genericJVM/compile",
  "config-typesafeJVM/compile",
  "config-shoconJVM/compile",
  "catsJVM/compile",
  "docs/tut",
  "readme/tut", "copyReadme", "checkDiff",
  "testsJVM/test",
  "tests-config-typesafeJVM/test",
  "tests-config-shoconJVM/test").mkString(";"))
addCommandAlias("validateJS", ";" + List(
  "coreJS/compile",
  "genericJS/compile",
  "config-shoconJS/compile",
  "catsJS/compile",
  "testsJS/test",
  "tests-config-shoconJS/test").mkString(";"))


lazy val core =
  module("core")
    .settings(yax.scala(file("modules/core_compat"), Compile))
lazy val coreJS = core.js
lazy val coreJVM = core.jvm


lazy val generic =
  module("generic")
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(
      "com.chuusai"  %%% "shapeless" % V.shapeless
    ))
    .settings(
      libraryDependencies += compilerPlugin("com.github.wheaties" %% "twotails" % V.twoTails cross CrossVersion.full),
      ivyConfigurations   += config("compile-only").hide,
      libraryDependencies += "com.github.wheaties" %% "twotails-annotations" % V.twoTails % "compile-only" cross CrossVersion.full,
      unmanagedClasspath in Compile ++= update.value.select(configurationFilter("compile-only"))
    )
lazy val genericJS = generic.js
lazy val genericJVM = generic.jvm


lazy val typesafeJVM =
  module("config-typesafe")
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(
      "com.typesafe"  % "config"    % V.typesafeConfig
    ))
    .settings(yax(file("modules/config"), Compile, "typesafe"))
    .jvm


lazy val shocon =
  module("config-shocon")
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(
      "eu.unicredit" %%% "shocon" % V.shocon
    ))
    .settings(yax(file("modules/config"), Compile, "shocon"))
lazy val shoconJS = shocon.js
lazy val shoconJVM = shocon.jvm


lazy val cats =
  module("cats")
    .dependsOn(core)
    .settings(libraryDependencies ++= Seq(
      "org.typelevel"  %%% "cats" % V.cats
    ))
lazy val catsJS = cats.js
lazy val catsJVM = cats.jvm



lazy val testsSettings = Seq(
  libraryDependencies ++= Seq(
    "org.scalacheck"             %%% "scalacheck"                % V.scalacheck,
    "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % V.scalacheckShapeless,
    "org.typelevel"              %%% "cats-laws"                 % V.cats
  ).map(_ % "test"))

lazy val allTests = (project in file(".all-tests"))
  .settings(noPublishSettings)
  .aggregate(testsJS, testsJVM)
  .aggregate(testsTypesafeJVM)
  .aggregate(testsShoconJS, testsShoconJVM)

lazy val testing =
  module("testing")
    .settings(noPublishSettings)
    .settings(libraryDependencies ++= Seq(
      "org.scalacheck"             %%% "scalacheck"                % V.scalacheck,
      "com.github.alexarchambault" %%% "scalacheck-shapeless_1.13" % V.scalacheckShapeless,
      "org.typelevel"              %%% "cats-laws"                 % V.cats
    ))
    .settings(
      coverageExcludedPackages := "classy\\.testing\\..*")
    .dependsOn(core, generic, cats)
lazy val testingJS = testing.js
lazy val testingJVM = testing.jvm

lazy val tests =
  module("tests", sourceConfig = Test)
    .settings(noPublishSettings)
    .settings(testsSettings: _*)
    .dependsOn(testing)
    .dependsOn(core, generic, cats)
lazy val testsJS = tests.js
lazy val testsJVM = tests.jvm

lazy val testsTypesafeJVM =
  module("tests-config-typesafe", sourceConfig = Test).jvm
    .settings(noPublishSettings)
    .settings(testsSettings: _*)
    .dependsOn(testingJVM)
    .dependsOn(coreJVM, genericJVM)
    .dependsOn(typesafeJVM)
    .settings(yax(file("modules/tests-config"), Test, "typesafe"))

lazy val testsShocon =
  module("tests-config-shocon", sourceConfig = Test)
    .settings(noPublishSettings)
    .settings(testsSettings: _*)
    .dependsOn(testing)
    .dependsOn(core, generic)
    .dependsOn(shocon)
    .settings(yax(file("modules/tests-config"), Test, "shocon"))
lazy val testsShoconJS = testsShocon.js
lazy val testsShoconJVM = testsShocon.jvm

lazy val docs =
  (project in file("modules/docs"))
    .enablePlugins(MicrositesPlugin)
    .dependsOn(coreJVM)
    .dependsOn(catsJVM)
    .dependsOn(genericJVM)
    .dependsOn(typesafeJVM)
    .settings(noPublishSettings)
    .settings(unidocSettings)
    .settings(ghpages.settings)
    .settings(
      scalacOptions in (Compile, doc) ++= Seq(
        "-implicits", "-implicits-show-all",
        "-groups",
        "-doc-source-url", scmInfo.value.get.browseUrl + "/tree/master€{FILE_PATH}.scala",
        "-sourcepath", baseDirectory.in(LocalRootProject).value.getAbsolutePath),
      tutScalacOptions ~= (_.filterNot(Set("-Yno-predef"))),
      micrositeName := "Case Classy",
      micrositeAuthor := "the contributors",
      micrositeHighlightTheme := "atom-one-light",
      micrositeBaseUrl := "/case-classy",
      micrositeGithubRepo := "case-classy",
      micrositePalette := Map(
        "brand-primary"   -> "#27607D",
        "brand-secondary" -> "#16212F",
        "brand-tertiary"  -> "#17283E",
        "gray-dark"       -> "#494A4F",
        "gray"            -> "#76767E",
        "gray-light"      -> "#E6E7EC",
        "gray-lighter"    -> "#F4F5F9",
        "white-color"     -> "#FFFFFF"),
      unidocProjectFilter in (ScalaUnidoc, unidoc) :=
        inProjects(coreJVM, genericJVM, catsJVM, typesafeJVM),
      autoAPIMappings := true,
      docsMappingsAPIDir := "api",
      addMappingsToSiteDir(mappings in (ScalaUnidoc, packageDoc), docsMappingsAPIDir),
      git.remoteRepo := "https://github.com/47deg/case-classy",
      includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
    )

lazy val docsMappingsAPIDir = settingKey[String](
  "Name of subdirectory in site target directory for api docs")


lazy val readme =
  (project in file("modules/readme"))
    .dependsOn(coreJVM)
    .dependsOn(catsJVM)
    .dependsOn(genericJVM)
    .dependsOn(typesafeJVM)
    .settings(noPublishSettings)
    .settings(tutSettings)
    .settings(
      tutScalacOptions ~= (_.filterNot(Set("-Yno-predef"))))

//
//

import org.scalajs.sbtplugin.cross.{ CrossProject, CrossType }

def module(
  modName: String,
  sourceConfig: Configuration = Compile
): CrossProject =
  CrossProject(modName, file(s"modules/.$modName"), CrossType.Dummy)
    .settings(name := s"classy-$modName")
    .settings(scalaSource in sourceConfig := baseDirectory.value.getParentFile.getParentFile / modName)
    .settings(unmanagedSourceDirectories in Compile      := Nil)
    .settings(unmanagedSourceDirectories in Test         := Nil)
    .settings(unmanagedSourceDirectories in sourceConfig := Seq((scalaSource in sourceConfig).value))
