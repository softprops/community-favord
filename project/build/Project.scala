import sbt._

class Project(info: ProjectInfo) extends AppengineProject(info)
  with DataNucleus {

  val uf_version = "0.3.0-SNAPSHOT"

  lazy val uff = "net.databinder" %% "unfiltered-filter" % uf_version
  lazy val ufj = "net.databinder" %% "unfiltered-jetty" % uf_version
  val ufjs = "net.databinder" %% "unfiltered-json" % uf_version

  lazy val dispatch_gae = "net.databinder" %% "dispatch-http-gae" % "0.8.0.Beta2"
  lazy val mu = "net.databinder" %% "dispatch-meetup" % "0.7.8"
  lazy val dbjs = "net.databinder" %% "dispatch-lift-json" % "0.7.8"

  // persistence
  lazy val jdo = "javax.jdo" % "jdo2-api" % "2.3-ea"

  val gae = "com.google.appengine" % "appengine-api-1.0-sdk" % "1.3.4"

  // testing
  lazy val uf_spec = "net.databinder" %% "unfiltered-spec" % uf_version % "test"
  lazy val jboss = "JBoss repository" at
    "https://repository.jboss.org/nexus/content/groups/public/"

}
