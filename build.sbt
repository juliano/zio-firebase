import org.scalafmt.sbt.ScalafmtPlugin.autoImport.scalafmtOnCompile

inThisBuild(
  List(
    organization := "io.github.juliano",
    homepage     := Some(url("https://github.com/juliano/zio-firebase")),
    developers := List(
      Developer("juliano", "Juliano Alves", "von.juliano@gmail.com", url("https://juliano-alves.com/"))
    ),
    scalafmtCheck     := true,
    scalafmtSbtCheck  := true,
    scalafmtOnCompile := !insideCI.value,
    version           := "0.0.8",
    versionScheme     := Some("always"),
    publishTo         := Some("GitHub juliano Apache Maven Packages" at "https://maven.pkg.github.com/juliano/zio-firebase"),
    publishMavenStyle := true,
    credentials += Credentials(
      "GitHub Package Registry",
      "maven.pkg.github.com",
      "juliano",
      System.getenv("GITHUB_TOKEN")
    )
  )
)

lazy val root = project
  .in(file("."))
  .settings(
    name         := "zio-firebase",
    scalaVersion := "3.5.0",
    libraryDependencies ++= Seq(
      "dev.zio"            %% "zio"               % "2.1.15",
      "dev.zio"            %% "zio-prelude"       % "1.0.0-RC39",
      "dev.zio"            %% "zio-interop-guava" % "32.1.0",
      "com.google.firebase" % "firebase-admin"    % "9.1.1",
      "dev.zio"            %% "zio-test"          % "2.1.15",
      "dev.zio"            %% "zio-test-sbt"      % "2.1.15" % Test
    ),
    testFrameworks += new TestFramework("zio.test.sbt.ZTestFramework"),
    scalacOptions := Seq(
      "-deprecation",
      "-encoding",
      "utf-8",
      "-explain-types",
      "-feature",
      "-unchecked",
      "-language:postfixOps",
      "-language:higherKinds",
      "-language:implicitConversions",
      "-language:existentials",
      "-Xkind-projector",
      "-Yretain-trees"
    )
  )
