organization := "com.github.catalystcode"
name := "twitter-stream-processing"
description := "Introduction to Big Data. Assignment 2. Stream Processing with Spark. Edinburgh team."

scalaVersion := "2.11.7"

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-feature"
)

val sparkVersion = "2.1.0"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "org.apache.spark" %% "spark-streaming" % sparkVersion
).map(_ % "compile")

libraryDependencies ++= Seq("org.apache.spark" %% "spark-mllib" % "2.3.2",
  "com.rometools" % "rome" % "1.8.0",
  "org.jsoup" % "jsoup" % "1.10.3",
  "log4j" % "log4j" % "1.2.17"
)

libraryDependencies ++= Seq(
  "org.mockito" % "mockito-core" % "2.8.47",
  "org.mockito" % "mockito-inline" % "2.8.47",
  "org.scalatest" %% "scalatest" % "2.2.1"
).map(_ % "test")

assemblyMergeStrategy in assembly := {                                          
 case PathList("META-INF", xs @ _*) => MergeStrategy.discard                    
 case x => MergeStrategy.first                                                  
}

mainClass in (Compile, run) := Some("Main")
mainClass in (Compile, packageBin) := Some("Main")
