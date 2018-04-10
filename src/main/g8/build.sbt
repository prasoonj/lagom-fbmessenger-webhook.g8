organization in ThisBuild := "$organization$"
version in ThisBuild := "$version$"

// the Scala version that will be used for cross-compiled libraries
scalaVersion in ThisBuild := "2.12.4"

val macwire = "com.softwaremill.macwire" %% "macros" % "2.3.0" % "provided"
val scalaTest = "org.scalatest" %% "scalatest" % "3.0.4" % Test
val scalaz = "org.scalaz" %% "scalaz-core" % "7.2.20"

lazy val `$name;format="norm"$` = (project in file("."))
  .aggregate(`webhooks-api`, `webhooks-impl`)


lazy val `webhooks-api` = (project in file("webhooks-api"))
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslApi,
      scalaz
    )
  )

lazy val `webhooks-impl` = (project in file("webhooks-impl"))
  .enablePlugins(LagomScala)
  .settings(
    libraryDependencies ++= Seq(
      lagomScaladslPersistenceCassandra,
      lagomScaladslKafkaBroker,
      lagomScaladslTestKit,
      macwire,
      scalaTest,
      scalaz
    )
  )
  .dependsOn(`webhooks-api`)
