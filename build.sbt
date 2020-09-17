// standard version
//scalaVersion := "0.27.0-RC1"

// if the current version is 0.27 then this is the nightly build
// the version is on the dotty website in the header
scalaVersion := "0.28.0-bin-20200915-5c422e2-NIGHTLY"

enablePlugins(ScalaJSPlugin)
name := "dotty scalajs"
scalaJSLinkerConfig ~= { _.withModuleKind(ModuleKind.CommonJSModule) }
scalaJSUseMainModuleInitializer := true
scalacOptions ++= Seq("-language:implicitConversions", "-Yindent-colons", "-Ycheck-init", "-Yexplicit-nulls", "-source", "3.1")
libraryDependencies ++= Seq(
    // keep this if you are building for a browser
    //("org.scala-js" %%% "scalajs-dom" % "1.1.0").withDottyCompat(scalaVersion.value)
)
Global / onChangedBuildSource := ReloadOnSourceChanges