### How to change existing sbt projet to adopt PlayFramework

Import the necessary dependencies:
1. In `plugins.sbt` add 
`addSbtPlugin("com.typesafe.play" % "sbt-plugin" % "2.8.1")`

2. In `build.sbt` add the below to `libraryDependencies`:
`"com.typesafe.play" %% "play" % playVersion`

2.1 Add `guice` support in the `libraryDependencies ++= Seq(guice)`
2.2 Add the below to include `/src` to classpath:
`unmanagedSourceDirectories in Compile += baseDirectory.value / "src"`

3. Add the `/src` directory to add it to classpath and verify:

```sbt "show unmanagedSourceDirectories"
[info] Loading global plugins from /home/training/.sbt/1.0/plugins
[info] Loading settings for project coursera-build from plugins.sbt ...
[info] Loading project definition from /home/training/IdeaProjects/coursera/project
[info] Loading settings for project root from build.sbt ...
[info] Set current project to coursera (in build file:/home/training/IdeaProjects/coursera/)
[info] * /home/training/IdeaProjects/coursera/app-2.12
[info] * /home/training/IdeaProjects/coursera/app
[info] * /home/training/IdeaProjects/coursera/app
[info] * /home/training/IdeaProjects/coursera/src
```

PlayFramework expects the following:
1. `controllers` in `app/controllers` - Add your controller.scala here
2. `application.conf` in `conf`
3. `routes` in `conf` - Add a route that refers to controller on step-1

Execute: `sbt run`


