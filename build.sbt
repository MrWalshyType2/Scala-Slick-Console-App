name := "qa-cinemas-terminal-app"

version := "0.1"

scalaVersion := "2.13.4"

resolvers += Resolver.sonatypeRepo("snapshots")
resolvers += Resolver.jcenterRepo

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest" % "3.2.2" % "test",
  "org.mockito" % "mockito-core" % "2.7.22" % "test",
  "org.scalatestplus" %% "selenium-3-141" % "3.2.2.0" % "test",
  "org.scalatest" %% "scalatest-flatspec" % "3.2.2" % "test",
  "org.scalatest" %% "scalatest-shouldmatchers" % "3.2.2" % "test",
  "org.seleniumhq.selenium" % "selenium-java" % "3.141.59",
  "org.scalactic" %% "scalactic" % "3.2.2",

  "com.typesafe.slick" %% "slick" % "3.3.3",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.slick" %% "slick-hikaricp" % "3.3.3",
  "mysql" % "mysql-connector-java" % "8.0.23",
  "com.h2database" % "h2" % "1.4.197",
)