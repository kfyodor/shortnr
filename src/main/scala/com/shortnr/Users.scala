package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._
import com.shortnr.AppDatabase
import java.util.UUID

case class User(id: Long, token: String)

object UserModel extends AppDatabase {
  def findOrCreate(id: Long) = {
    val q = for { user <- Users() if user.id === id } yield user.token

    q.list match {
      case List(token) => token
      case _           => create(id)
    }
  }

  def create(id: Long) = {
    (Users() returning Users().map(_.token)) += User(id, createToken())
  }

  def createToken() = UUID.randomUUID().toString
}


class Users(tag: Tag) extends Table[User](tag, "users") {
  def id    = column[Long]("UID", O.PrimaryKey)
  def token = column[String]("TOKEN")

  def tokenIdx = index("token_idx", token, unique = true)

  def * = (id, token) <> (User.tupled, User.unapply _)
}

object Users {
  def apply() = TableQuery[Users]
}