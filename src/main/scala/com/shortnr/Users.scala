package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

case class User(id: Int, token: String)

class Users(tag: Tag) extends Table[User](tag, "users") {
  def id    = column[Int]("UID", O.PrimaryKey, O.AutoInc)
  def token = column[String]("TOKEN")
  def *     = (id, token) <> (User.tupled, User.unapply _)
}

object Users {
  def apply() = TableQuery[Users]
}