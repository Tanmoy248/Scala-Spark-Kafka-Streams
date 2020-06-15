val sparkVersion = "3.0.0-preview2"
val playVersion="2.8.1"
val jacksonVersion="2.10.1"

lazy val root = (project in file("."))
  .enablePlugins(PlayScala)
  .settings(
    name := "coursera",
    organization := "com.tanmoy248",
    version := "0.1.0",
scalaVersion := "2.12.10",
mainClass := Some("Main"),


//lazy val root = (project in file(".")).enablePlugins(play.PlayScala)

//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion
//dependencyOverrides += "com.fasterxml.jackson.core" % "jackson-databind" % jacksonVersion
//dependencyOverrides += "com.fasterxml.jackson.module" % "jackson-module-scala_2.11"

libraryDependencies ++= Seq(
  guice,
  "org.scala-lang" % "scala-library" % scalaVersion.toString(),
  "org.apache.spark" %% "spark-streaming" % sparkVersion,
  "org.apache.spark" %% "spark-core" % sparkVersion,
  "org.apache.spark" %% "spark-sql" % sparkVersion,
  "com.typesafe.play" %% "play-json" % playVersion,
  // https://mvnrepository.com/artifact/org.apache.spark/spark-streaming-kafka-0-10
  "org.apache.spark" %% "spark-streaming-kafka-0-10" % sparkVersion,
  // https://mvnrepository.com/artifact/org.mongodb/casbah
  "org.mongodb" %% "casbah" % "3.1.1" pomOnly(),
  // https://mvnrepository.com/artifact/com.typesafe/config
  "com.typesafe" % "config" % "1.2.1",
  // add play framework support
  "com.typesafe.play" %% "play" % playVersion,
  // https://mvnrepository.com/artifact/org.mongodb.scala/mongo-scala-driver
  "org.mongodb.scala" %% "mongo-scala-driver" % "2.7.0"

)
  )

assemblyMergeStrategy in assembly := {
  case PathList("META-INF", xs @ _*) => MergeStrategy.discard
  case x => MergeStrategy.first
}

unmanagedSourceDirectories in Compile += baseDirectory.value / "src"
unmanagedSourceDirectories in Compile += baseDirectory.value / "conf"

