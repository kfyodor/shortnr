package com.shortnr.tables

import scala.annotation.tailrec
import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.{ AppDatabase, Helper }
import com.shortnr.tables._

case class Link(id: Long, url: String, code: String, folderId: Option[Long], userId: Long) {
  def doClick(referer: String, remoteIp: String): String = {
    ClickModel.create(this, referer, remoteIp)
    return url
  }
}

object LinkModel extends AppDatabase {
  def create(user: User, url: String, code: Option[String], folderId: Option[Long]): Link = {
    (Links() returning Links()) += 
      Link(0, url, generateCode(), folderId, user.id)
  }

  def forUser(user: User): List[Link] = {
    Links().filter(_.userId === user.id).list
  }

  def findByCode(code: String): Option[Link] = {
    Links().filter(_.code === code).firstOption
  }

  def getOrGenerateCode(code: Option[String]): String = {
    code getOrElse generateUniqueCode()
  }

  def generateCode(): String = {
    Helper.generateRandomString(8)
  }

  @tailrec
  def generateUniqueCode(): String = {
    val code = generateCode()

    Links().filter(_.code === code).length.run match {
      case 0 => code
      case _ => generateUniqueCode()
    }
  }
}

class Links(tag: Tag) extends Table[Link](tag, "links") {
  def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def url      = column[String]("url")
  def code     = column[String]("code")
  def folderId = column[Option[Long]]("folder_id")
  def userId   = column[Long]("user_id")

  def user = foreignKey("user_fk", userId, Users())(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def folder = foreignKey("folder_fk", folderId, Folders())(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)

  def userIdx = index("user_idx", userId)
  def folderIdx = index("folder_idx", folderId)
  
  def codeIdx = index("code_idx", code, unique = true)

  def * = (id, url, code, folderId, userId) <> (Link.tupled, Link.unapply _)
}

object Links {
  def apply() = TableQuery[Links]
}