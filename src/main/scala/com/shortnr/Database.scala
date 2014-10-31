package com.shortnr

import scala.slick.jdbc.GetResult

import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.{ Config => C }
import com.shortnr.tables._

trait AppDatabase {
  def db =
    Database.forURL(
    s"jdbc:postgresql://${C.dbHost}:${C.dbPort}/${C.dbName}",
    user     = C.dbUser,
    password = C.dbPassword,
    driver   = "org.postgresql.Driver"
  )

  implicit def session: Session = db.createSession()

  val ddl = Seq(Users(), Folders(), Links(), Clicks()).map(_.ddl).reduce(_ ++ _)

  def dropTables   = ddl.drop
  def createTables = ddl.create
}

