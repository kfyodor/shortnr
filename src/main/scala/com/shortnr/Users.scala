package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.AppDatabase
import com.shortnr.helpers._
import com.shortnr.auth.UserAuthorizationChecks

import com.roundeights.hasher.Hasher

case class User(
  id:     Long, 
  token:  String, 
  salt:   String, 
  secret: String
) extends UserAuthorizationChecks

case class UserToken(token: String)

object UserModel extends AppDatabase {
  implicit def stringToToken(token: String): UserToken = UserToken(token)

  val users = Users()

  def findByToken(token: String): Option[User] =
    users.filter(_.token === token).firstOption

  def findById(id: Long): Option[User] =
    users.filter(_.id === id).firstOption

  def create(id: Long, secret: String): User = {
    val (salt, secureSecret) = hashSecret(secret)
    
    (users returning users) += 
      User(id, createToken, salt, secureSecret)
  }

  private def createToken = Helper.generateToken

  def authenticateOrCreate(id: Long, secret: String): UserToken = {
    authenticate(id, secret) { (id, secret) => 
      Some(create(id, secret)) 
    }.map(_.token).get 
  }

  def authenticate(id: Long, secret: String)(notAuthenticated: (Long, String) => Option[User]): Option[User] =
    findById(id) match {
      case Some(user) => Some(user).filter(checkSecret(secret) _)
      case None       => notAuthenticated(id, secret) // Creating user here is just for demo purposes.
    }

  private def hashSecret(secret: String): (String, String) = {
    val salt         = Helper.generateRandomString(16)
    val secureSecret = Hasher(secret).salt(salt).bcrypt.hex

    (salt, secureSecret)
  }

  private def checkSecret(secret: String)(user: User): Boolean =
    (Hasher(secret).salt(user.salt).bcrypt.hash= user.secret)
}


class Users(tag: Tag) extends Table[User](tag, "users") {
  def id     = column[Long]("id", O.PrimaryKey)
  def token  = column[String]("token")
  def salt   = column[String]("salt")
  def secret = column[String]("secret", O.DBType("VARCHAR(120)"))
  def tokenIdx = index("token_idx", token, unique = true)

  def * = (id, token, salt, secret) <> 
    (User.tupled, User.unapply _)
}

object Users {
  def apply() = TableQuery[Users]
}