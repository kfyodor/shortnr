package com.shortnr.tables

import scala.annotation.tailrec
import scala.slick.driver.PostgresDriver.simple._
import scala.util.Random

import com.shortnr.AppDatabase
import com.shortnr.tables._

case class Link(url: String, code: String, folderId: Option[Long], userId: Long)

object LinkModel extends AppDatabase {
  def create(user: User, url: String, code: Option[String], folderId: Option[Long]): Link = {
    (Links() returning Links()) += Link(url, generateCode(), folderId, user.id)
  }

  def getOrGenerateCode(code: Option[String]): String = {
    code match {
      case Some(code) => code
      case None       => generateCode()
    }
  }

  @tailrec
  def generateCode(): String = {
    val code = (new Random).alphanumeric.take(8).map(_.toLower).mkString
    val link = for {
      l <- Links() if l.code === code
    } yield l

    link.length.run match {
      case 0 => code
      case _ => generateCode()
    }
  }
}

class Links(tag: Tag) extends Table[Link](tag, "links") {
  def url      = column[String]("URL")
  def code     = column[String]("CODE")
  def folderId = column[Option[Long]]("FOLDER_ID")
  def userId   = column[Long]("USER_ID")
  
  def codeInx = index("code_idx", code, unique = true)

  def * = (url, code, folderId, userId) <> (Link.tupled, Link.unapply _)
}

object Links {
  def apply() = TableQuery[Links]
}