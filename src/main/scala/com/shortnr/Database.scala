package com.shortnr

import scala.slick.driver.PostgresDriver
import scala.slick.driver.PostgresDriver.simple._

import scala.slick.jdbc.meta.MTable

import com.shortnr.{ Config => C }
import com.shortnr.tables._

trait AppDatabase {
  def db = Database.forURL(
    s"jdbc:postgresql://${C.dbHost}:${C.dbPort}/${C.dbName}",
    user     = C.dbUser,
    password = C.dbPassword,
    driver   = "org.postgresql.Driver"
  )

  implicit val session: Session = db.createSession()

  def startDB() = {
    Users().ddl.create
  }
}