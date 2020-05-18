name := "coursera"

version := "0.1"

scalaVersion := "2.12.10"
val sparkVersion = "3.0.0-preview2"
val playVersion="2.8.1"

val jacksonVersion="2.10.1"

//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
//dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.typesafe.play" %% "play-json" % playVersion,
  // https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kafka-0-10
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion


)
