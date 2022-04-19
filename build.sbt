name := """payment-provider-app"""
organization := "tiendanube"

version := "1.0-SNAPSHOT"
scalaVersion := "2.13.8"

lazy val root = (project in file(".")).enablePlugins(PlayScala)
lazy val scalaTestVersion = "3.2.2"

libraryDependencies ++= Seq(
  guice,
  ws,

  // Test dependencies
  "org.scalatestplus.play" %% "scalatestplus-play" % "5.0.0" % Test,
  "de.leanovate.play-mockws" %% "play-mockws" % "2.8.0" % Test
)

scalacOptions := Seq(
  "-encoding",
  "UTF-8",
  "-target:jvm-1.8",
  "-deprecation",
  "-feature",
  "-unchecked",
  "-language:implicitConversions",
  "-language:postfixOps",
  "-Ywarn-unused",
  // "-Xfatal-warnings"
)

// Adds additional packages into Twirl
//TwirlKeys.templateImports += "tiendanube.controllers._"

// Adds additional packages into conf/routes
// play.sbt.routes.RoutesKeys.routesImport += "tiendanube.binders._"
