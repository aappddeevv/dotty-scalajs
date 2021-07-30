// standard version
//scalaVersion := "0.27.0-RC1"
//scalaVersion := dottyLatestNightlyBuild.get
//scalaVersion := "3.0.0-M1-bin-20201022-b26dbc4-NIGHTLY"

inThisBuild(
  List(
    scalaVersion := "3.0.0-M1",
    scalacOptions ++= Seq(
      "-language:implicitConversions",
      "-Yindent-colons",
      "-Ycheck-init",
      "-Yexplicit-nulls"
      // only need this for 3.0/3.1 cross-compiling to 2.X, enables * for ?
      ,"-Ykind-projector"
      //"-source",
      //"3.1"
    )
  )
)

val jsSettings = Seq(
  scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) },
  scalaJSUseMainModuleInitializer := true,
  libraryDependencies ++= Seq(
    //("dev.zio" %%% "zio" % "1.0.3").withDottyCompat(scalaVersion.value),
    //("dev.zio" %%% "zio" % "1.0.3").withDottyCompat("0.27.0-RC1"),
    "com.github.rssh" %%% "dotty-cps-async" % "0.3.1-M1" //"0.3.0-SNAPSHOT"
    //("dev.zio" %%% "zio-query" % "0.2.5").withDottyCompat(scalaVersion.value),
  )
)

Global / onChangedBuildSource := ReloadOnSourceChanges

lazy val root = project
  .in(file("."))
  .aggregate(main)

lazy val main = project
  .in(file("main"))
  .settings(jsSettings)
  .enablePlugins(ScalaJSPlugin)
