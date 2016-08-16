/* -
 * Case Classy [case-classy]
 */

import sbt.Keys._
import sbt._

import scalariform.formatter.preferences._
import com.typesafe.sbt.SbtScalariform
import com.typesafe.sbt.SbtScalariform.ScalariformKeys
import de.heikoseeberger.sbtheader.AutomateHeaderPlugin
import de.heikoseeberger.sbtheader.HeaderPattern
import de.heikoseeberger.sbtheader.HeaderPlugin
import de.heikoseeberger.sbtheader.HeaderKey.headers

import scala.{ Console ⇒ C }

object BuildCommon extends AutoPlugin {

  override def requires = plugins.JvmPlugin && SbtScalariform && HeaderPlugin
  override def trigger = allRequirements

  override def projectSettings =
    baseSettings ++
      formatSettings ++
      enhancingScalaSettings ++
      miscSettings ++
      AutomateHeaderPlugin.projectSettings

  private[this] def baseSettings = Seq(
    scalaVersion := "2.11.8",

    organization := "com.47deg",

    fork in run := true,
    fork in Test := false,
    outputStrategy := Some(StdoutOutput),
    connectInput in run := true,
    cancelable in Global := true,

    scalacOptions ++= Seq(
      "-deprecation", "-feature", "-unchecked", "-encoding", "utf8"),
    scalacOptions ++= Seq(
      "-Ywarn-unused-import"),
    scalacOptions ++= Seq(
      "-language:implicitConversions",
      "-language:higherKinds"),

    javacOptions ++= Seq("-encoding", "UTF-8", "-Xlint:-options"),
    headers <<= (name, version) { (name, version) ⇒
      Map(
        "scala" → (
          HeaderPattern.cStyleBlockComment,
          s"""|/* -
           | * Case Classy [$name]
           | */
           |
           |""".stripMargin)
      )
    }
  )

  private[this] def enhancingScalaSettings = Seq(
    resolvers += Resolver.sonatypeRepo("releases"),
    libraryDependencies ++= Seq(
      // kind projector
      compilerPlugin(
        "org.spire-math" %% "kind-projector" % "0.8.0" cross CrossVersion.binary)
    )
  )

  private[this] def formatSettings = SbtScalariform.scalariformSettings ++ Seq(
    ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(SpacesAroundMultiImports, true)
      .setPreference(PreserveSpaceBeforeArguments, true)
      .setPreference(DanglingCloseParenthesis, Preserve)
      .setPreference(AlignArguments, true)
      .setPreference(AlignSingleLineCaseStatements, true)
      //.setPreference(DoubleIndentMethodDeclaration, true)
      .setPreference(MultilineScaladocCommentsStartOnFirstLine, true)
      .setPreference(PlaceScaladocAsterisksBeneathSecondAsterisk, true)
      .setPreference(RewriteArrowSymbols, true)
  )

  private[this] def miscSettings = Seq(
    shellPrompt := (s ⇒
      s"${C.BLUE}${Project.extract(s).currentProject.id}🤖 ${C.RESET} ")
  )

}
