organization  := "com.shortnr"

version       := "0.1"

scalaVersion  := "2.11.2"

scalacOptions := Seq("-unchecked", "-deprecation", "-encoding", "utf8")

parallelExecution in Test := false

resolvers ++= Seq(
  "RoundEights"        at "http://maven.spikemark.net/roundeights",
  "jBCrypt Repository" at "http://repo1.maven.org/maven2/org/"
)

libraryDependencies ++= {
  val akkaV = "2.3.6"
  val sprayV = "1.3.2"
  Seq(
    "io.spray"            %%  "spray-can"       % sprayV,
    "io.spray"            %%  "spray-routing"   % sprayV,
    "io.spray"            %%  "spray-json"      % "1.3.1",
    "io.spray"            %%  "spray-testkit"   % sprayV  % "test",
    "com.typesafe.akka"   %%  "akka-actor"      % akkaV,
    "com.typesafe.akka"   %%  "akka-testkit"    % akkaV   % "test",
    "com.typesafe.slick"  %%  "slick"           % "2.1.0",
    "org.specs2"          %%  "specs2-core"     % "2.3.11" % "test",
    "org.postgresql"      %   "postgresql"      % "9.3-1100-jdbc4",
    "ch.qos.logback"      %   "logback-classic" % "1.0.12",
    "com.roundeights"     %%  "hasher"          % "1.0.0",
    "org.mindrot"         %   "jbcrypt"         % "0.3m",
    "com.mchange" % "c3p0" % "0.9.2.1"
  )
}

Revolver.settings
