package com.shortnr

import scala.slick.jdbc.GetResult

import scala.slick.driver.PostgresDriver.simple._
import com.mchange.v2.c3p0._;

import com.shortnr.{ Config => C }
import com.shortnr.tables._

trait AppDatabase {
  def db = {
    val ds = new ComboPooledDataSource

    ds.setDriverClass("org.postgresql.Driver")
    ds.setUser(C.dbUser)
    ds.setPassword(C.dbPassword)
    ds.setJdbcUrl(s"jdbc:postgresql://${C.dbHost}:${C.dbPort}/${C.dbName}")

    Database.forDataSource(ds)
  }
  //   Database.forURL(
  //   s"jdbc:postgresql://${C.dbHost}:${C.dbPort}/${C.dbName}",
  //   user     = C.dbUser,
  //   password = C.dbPassword,
  //   driver   = "org.postgresql.Driver"
  // )

  implicit def session: Session = db.createSession()

  val ddl = Seq(Users(), Folders(), Links(), Clicks()).map(_.ddl).reduce(_ ++ _)

  def dropTables   = ddl.drop
  def createTables = ddl.create
}

