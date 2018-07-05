javacOptions ++= Seq("-source", "1.8")

lazy val root = (project in file("."))
  .settings(
    name := "TDFishers",
    scalaVersion := "2.12.6",
    libraryDependencies ++= Seq(
      "org.scalatest" %% "scalatest" % "3.0.5" % "test",
      "org.jfree" % "jfreechart" % "1.0.17",
      "org.jfree" % "jcommon" % "1.0.21",
      "com.lowagie" % "itext" % "1.2.3",
      "org.beanshell" % "bsh" % "2.0b4",
      "edu.gmu.cs" % "mason" % "19.0" from "https://bintray.com/wjtan/maven/download_file?file_path=cs%2Fgmu%2Fedu%2Fmason%2F19.0%2Fmason.19.jar"
    )
  )
