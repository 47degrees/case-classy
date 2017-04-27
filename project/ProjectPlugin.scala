import com.typesafe.sbt.GitPlugin.autoImport._
import com.typesafe.sbt.site.SitePlugin.autoImport._
import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import microsites.MicrositesPlugin.autoImport._
import org.scalajs.sbtplugin.ScalaJSPlugin.autoImport._
import org.scalajs.sbtplugin.cross.{CrossProject, CrossType}
import sbt.Keys._
import sbt._
import sbtorgpolicies.OrgPoliciesKeys.orgBadgeListSetting
import sbtorgpolicies.OrgPoliciesPlugin
import sbtorgpolicies.OrgPoliciesPlugin.autoImport._
import sbtorgpolicies.model.Dev
import sbtorgpolicies.runnable.syntax._
import sbtorgpolicies.templates._
import sbtorgpolicies.templates.badges._
import sbtunidoc.ScalaUnidocPlugin.autoImport._
import scoverage.ScoverageKeys
import tut.Plugin._

object ProjectPlugin extends AutoPlugin {

  override def trigger: PluginTrigger = allRequirements

  override def requires: Plugins = OrgPoliciesPlugin

  object autoImport {

    lazy val V = new {
      lazy val typesafeConfig = "1.3.0"
      lazy val twoTails = "0.3.1"
    }

    def module(
      modName: String,
      hideFolder: Boolean = false
    ): CrossProject =
      CrossProject(modName, file(s"""modules/${if (hideFolder) "." else ""}$modName"""), CrossType.Pure)
        .settings(moduleName := s"classy-$modName")

    def jvmModule(modName: String): Project =
      Project(modName, file(s"""modules/$modName"""))
        .settings(moduleName := s"classy-$modName")

    lazy val testsSettings = Seq(
      %%("cats-laws"),
      %%("scalacheck"),
      %%("scheckShapeless")
    )

    lazy val docsMappingsAPIDir: SettingKey[String] = settingKey[String](
      "Name of subdirectory in site target directory for api docs")

    lazy val docsSettings = Seq(
      scalacOptions in(Compile, doc) ++= Seq(
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
        "brand-primary" -> "#27607D",
        "brand-secondary" -> "#16212F",
        "brand-tertiary" -> "#17283E",
        "gray-dark" -> "#494A4F",
        "gray" -> "#76767E",
        "gray-light" -> "#E6E7EC",
        "gray-lighter" -> "#F4F5F9",
        "white-color" -> "#FFFFFF"),
      autoAPIMappings := true,
      docsMappingsAPIDir in ScalaUnidoc := "api",
      addMappingsToSiteDir(mappings in(ScalaUnidoc, packageDoc), docsMappingsAPIDir in ScalaUnidoc),
      git.remoteRepo := "https://github.com/47deg/case-classy",
      includeFilter in makeSite := "*.html" | "*.css" | "*.png" | "*.jpg" | "*.gif" | "*.js" | "*.swf" | "*.yml" | "*.md"
    )

    lazy val readmeSettings: Seq[Def.Setting[_]] = tutSettings ++ Seq(
      tutScalacOptions ~= (_.filterNot(Set("-Yno-predef"))),
      tutSourceDirectory :=
        (baseDirectory in LocalRootProject).value / "modules" / "readme" / "src" / "main" / "tut",
      tutTargetDirectory := baseDirectory.value.getParentFile.getParentFile
    )
  }

  lazy val commandAliases: Seq[Def.Setting[_]] =
    addCommandAlias("validate", ";validateJS;validateJVM") ++
      addCommandAlias("validateDocs", List("docs/tut", "readme/tut", "project root").asCmd) ++
      addCommandAlias("validateCoverage", ";coverage;validateJVM;coverageReport;coverageAggregate;coverageOff") ++
      addCommandAlias("validateJVM", List(
        "clean",
        "coreJVM/compile",
        "genericJVM/compile",
        "config-typesafeJVM/compile",
        "config-shoconJVM/compile",
        "catsJVM/compile",
        "testsJVM/test",
        "tests-config-typesafeJVM/test",
        "tests-config-shoconJVM/test",
        "project root").asCmd) ++
      addCommandAlias("validateJS", List(
        "clean",
        "coreJS/compile",
        "genericJS/compile",
        "config-shoconJS/compile",
        "catsJS/compile",
        "testsJS/test",
        "tests-config-shoconJS/test",
        "project root").asCmd)

  override def projectSettings: Seq[Def.Setting[_]] = commandAliases ++ Seq(

    name := "case-classy",
    orgProjectName := "Case Classy",
    description := "configuration with less hassle",
    startYear := Option(2017),

    orgBadgeListSetting := List(
      TravisBadge.apply(_),
      CodecovBadge.apply(_),
      { info => MavenCentralBadge.apply(info.copy(libName = "classy")) },
      LicenseBadge.apply(_),
      ScalaLangBadge.apply(_),
      ScalaJSBadge.apply(_),
      GitHubIssuesBadge.apply(_)
    ),
    orgSupportedScalaJSVersion := Some("0.6.15"),
    orgScriptTaskListSetting := List(
      orgValidateFiles.asRunnableItem,
      "root/unidoc".asRunnableItemFull,
      "validateDocs".asRunnableItemFull,
      "validateJS".asRunnableItemFull,
      "validateCoverage".asRunnableItemFull
    ),
    orgUpdateDocFilesSetting +=
      (baseDirectory in LocalRootProject).value / "modules" / "readme" / "src" / "main" / "tut",
    orgEnforcedFilesSetting ~= (_ filterNot (_ == ScalafmtFileType)),
    orgMaintainersSetting += Dev("andyscott",
      Some("Andy Scott (twitter: [@andygscott](https://twitter.com/andygscott))"),
      Some("andy.g.scott@gmail.com")),


    fork in run := true,
    fork in Test := !isScalaJSProject.value,
    parallelExecution in Test := false,
    outputStrategy := Some(StdoutOutput),
    connectInput in run := true,
    cancelable in Global := true,

    scalaOrganization := "org.typelevel",
    scalaVersion := "2.12.1",
    crossScalaVersions := Seq("2.11.8", "2.12.1"),

    scalacOptions ++= Seq(
      "-Xfatal-warnings",
      "-Ywarn-unused-import",
      "-Yno-predef",
      "-Ypartial-unification"),

    libraryDependencies ++= (CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) => Seq(
        compilerPlugin("com.milessabin" % "si2712fix-plugin" % "1.2.0" cross CrossVersion.full))
      case Some((2, 12)) => Nil
      case _ => Nil
    }),

    scalacOptions in(Compile, doc) :=
      (scalacOptions in(Compile, doc)).value.filter(_ != "-Xfatal-warnings"),

    ScoverageKeys.coverageFailOnMinimum := false
  ) ++ AutomateHeaderPlugin.projectSettings

  implicit class CommandAliasOps(command: String) {

    def asCmd: String =
      if (command.contains("/")) s";project ${command.replaceAll("/", ";")}"
      else s";$command"

  }

  implicit class CommandAliasListOps(commandList: List[String]) {

    def asCmd: String = commandList.map(_.asCmd).mkString("")

  }

}
