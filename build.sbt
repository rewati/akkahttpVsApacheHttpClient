organization := "com.ytel"

name := "loadtester"

version := "0.1.0-SNAPSHOT"

scalaVersion := "2.12.4"

val repoResolvers = new {
  val maven = "Maven Repository" at "http://mvnrepository.com/artifact"
  val mavenCental = "Maven Central" at "https://repo1.maven.org/maven2"
  val ytelInternal = "Ytel internal" at "http://artifactsrv.ytel.com:8080/repository/internal/"
  val ytelSnapShot = "Ytel internal" at "http://artifactsrv.ytel.com:8080/repository/snapshots/"
  val common = Seq(maven,mavenCental,ytelInternal,ytelSnapShot)
}

resolvers ++= repoResolvers.common
libraryDependencies += "com.ytel" %% "miefus" % "2.3"
libraryDependencies += "org.apache.commons" % "commons-lang3" % "3.6"
libraryDependencies += "org.apache.httpcomponents" % "httpasyncclient" % "4.1.2"
libraryDependencies += "commons-io" % "commons-io" % "2.5"

assemblyMergeStrategy in assembly := {
  case "reference.conf" => MergeStrategy.concat
  case x if x.contains(".properties") => MergeStrategy.last
  case PathList("org.slf4j", "impl", "versions.properties", xs @ _*) => MergeStrategy.first
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

mainClass in assembly := Some("com.ytel.myapp.Application")
