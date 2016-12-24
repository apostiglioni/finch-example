name := "rest-scala"

version := "1.0"

scalaVersion := "2.11.7"

resolvers ++= Seq(
   "Twitter Maven Repository" at "http://maven.twttr.com"
,  "Artima Maven Repository"  at "http://repo.artima.com/releases" // required by `com.artima.supersafe` plugin
)

def dependency(groupId: String)(artifactId: String)(version: String) = groupId %% artifactId % version
def testDependency(groupId: String)(artifactId: String)(version: String) = dependency(groupId)(artifactId)(version) % "test"

def circe     (artifactId: String) = dependency("io.circe")(artifactId)("0.6.1")
def finch     (artifactId: String) = dependency("com.github.finagle")(artifactId)("0.11.0")
def scalaz    (artifactId: String) = dependency("org.scalaz")(artifactId)("7.2.7")
def cats      (artifactId: String) = dependency("org.typelevel")(artifactId)("0.8.1")
val scalactic                      = dependency("org.scalactic")("scalactic")("3.0.1")
val scalatest                      = testDependency("org.scalatest")("scalatest")("3.0.1")
val slick                          = dependency("com.typesafe.slick")("slick")("3.1.1")
val slf4j                          = "org.slf4j" % "slf4j-nop" % "1.6.4"
val h2                             = "com.h2database" % "h2" % "1.4.193"

val circeD = Seq("circe-core", "circe-generic", "circe-jawn") map ("io.circe"           %%     _  % "0.6.1")
val finchD = Seq("finch-core", "finch-circe")                 map ("com.github.finagle" %%     _  % "0.11.0")
val catsD  = Seq                                                  ("org.typelevel"      %% "cats" % "0.8.1")

libraryDependencies ++= Seq(
  circe("circe-core")
, circe("circe-generic")
, circe("circe-jawn")
, finch("finch-core")
, finch("finch-circe")
, cats ("cats")
, scalactic
, scalatest
, slick
, slf4j
, h2
)
