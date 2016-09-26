//

lazy val root = (project in file("."))
  .aggregate(allClassy: _*)
  .aggregate(tests)
  .aggregate(tuts)

lazy val catsVersion       = "0.7.0"
lazy val specs2Version     = "3.8.4"
lazy val scalacheckVersion = "1.13.2"

lazy val useRootForSrc =
  scalaSource in Compile := baseDirectory.value.getParentFile.getParentFile / "src"

lazy val allClassy: Seq[ProjectReference] =
  Seq(`case-classy`, `case-classy-typesafe`, `case-classy-knobs`)

lazy val `case-classy` = (project in file("target/.case-classy"))
  .settings(name := "case-classy", useRootForSrc,
    excludeFilter in unmanagedSources := HiddenFileFilter || "*.*.scala")
  .settings(libraryDependencies ++=
    Seq(
      "org.typelevel"     %% "cats-core"              % catsVersion,
      "com.chuusai"       %% "shapeless"              % "2.3.1"
    ))

lazy val `case-classy-typesafe` = (project in file("target/.case-classy-typesafe"))
  .settings(name := "case-classy-typesafe", useRootForSrc,
    includeFilter in unmanagedSources := HiddenFileFilter || "*.typesafe.scala")
  .dependsOn(`case-classy`)
  .settings(libraryDependencies ++=
    Seq(
      "com.typesafe"       % "config"                 % "1.3.0"
    ))

lazy val `case-classy-knobs` = (project in file("target/.case-classy-knobs"))
  .settings(name := "case-classy-knobs", useRootForSrc,
    includeFilter in unmanagedSources := HiddenFileFilter || "*.knobs.scala")
  .dependsOn(`case-classy`)
  .settings(resolvers += Resolver.bintrayRepo("oncue", "releases"))
  .settings(libraryDependencies ++=
    Seq(
      "oncue.knobs"       %% "core"                   % "3.8.107"
    ))

lazy val tests = (project in file("target/.tests"))
  .settings(name := "tests",
    scalaSource in Test := baseDirectory.value.getParentFile.getParentFile / "tests")
  .settings(noPublishSettings)
  .dependsOn(allClassy.map(_ % "compile"): _*)
  .settings(libraryDependencies ++=
    Seq(
      "org.specs2"        %% "specs2-core"            % specs2Version,
      "org.specs2"        %% "specs2-cats"            % specs2Version,
      "org.scalacheck"    %% "scalacheck"             % scalacheckVersion
    ).map(_ % "test"))

lazy val tuts = (project in file("target/.tuts"))
  .settings(name := "tuts")
  .settings(noPublishSettings)
  .settings(tutSettings)
  .settings(tutSourceDirectory := baseDirectory.value.getParentFile.getParentFile / "tuts")
  .dependsOn(allClassy.map(_ % "compile"): _*)
