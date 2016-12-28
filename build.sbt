//

lazy val root = (project in file("."))
  .aggregate(core)
  //.aggregate(generic)
  .aggregate(typesafe)
  //.aggregate(cats)
  .aggregate(tests)
  //.aggregate(docs)

lazy val V = new {
  lazy val cats                = "0.8.1"
  lazy val typesafeConfig      = "1.3.0"
  lazy val scalacheck          = "1.13.4"
  lazy val scalacheckShapeless = "1.1.4"
  lazy val shapeless           = "2.3.2"
}

def module(modName: String, sourceConfig: Configuration = Compile) =
  Project(modName, file(s"modules/.$modName"))
    .settings(name := s"case-classy-$modName")
    .settings(scalaSource in sourceConfig := baseDirectory.value.getParentFile / modName)

def crossPaths(paths: SettingKey[Seq[File]]): Setting[Seq[File]] =
  paths ++= paths.value.flatMap(path =>
    CrossVersion.partialVersion(scalaVersion.value) match {
      case Some((2, 11)) => Some(new File(path.getPath + "_2.11"))
      case Some((2, 12)) => Some(new File(path.getPath + "_2.12"))
      case _             => None
    })

lazy val core = module("core")
  .settings(crossPaths(unmanagedSourceDirectories in Compile))

lazy val generic = module("generic")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "com.chuusai"  %% "shapeless" % V.shapeless
  ))

lazy val typesafe = module("typesafe")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe"  % "config"    % V.typesafeConfig
  ))

lazy val cats = module("cats")
  .dependsOn(core)
  .settings(libraryDependencies ++= Seq(
    "org.typelevel"  %% "cats" % V.cats
  ))

lazy val tests = module("tests", Test)
  .dependsOn(core)
  .dependsOn(typesafe)
  .dependsOn(generic)
  .settings(libraryDependencies ++= Seq(
    "com.typesafe"        % "config"        % V.typesafeConfig,
    "org.scalacheck"     %% "scalacheck"    % V.scalacheck,
    "com.github.alexarchambault" %% "scalacheck-shapeless_1.13" % V.scalacheckShapeless
  ).map(_ % "test"))

lazy val docs = module("docs")
  .settings(noPublishSettings)
  .settings(tutSettings)
