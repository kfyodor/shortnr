package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.AppDatabase
import com.shortnr.serialization._

import java.util.UUID

case class User(id: Long, token: String)
case class UserToken(token: String)

object UserModel extends AppDatabase {
  implicit def stringToToken(token: String): UserToken = UserToken(token)

  def findByToken(token: String): Option[User] = {
    Users().filter(_.token === token).firstOption
  }

  def findOrCreate(id: Long): UserToken = {
    UserToken(
      Users().filter(_.id === id).map(_.token).firstOption.getOrElse(create(id))
    )
  }

  def create(id: Long) = {
    (Users() returning Users().map(_.token)) += User(id, createToken())
  }

  def createToken() = UUID.randomUUID().toString
}


class Users(tag: Tag) extends Table[User](tag, "users") {
  def id    = column[Long]("id", O.PrimaryKey)
  def token = column[String]("token")

  def tokenIdx = index("token_idx", token, unique = true)

  def * = (id, token) <> (User.tupled, User.unapply _)
}

object Users {
  def apply() = TableQuery[Users]
}