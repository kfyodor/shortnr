package com.shortnr.tables

import scala.annotation.tailrec
import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.AppDatabase
import com.shortnr.tables._
import com.shortnr.helpers._

case class LinkUrl(url: String)

case class Link(
  id:       Long,
  url:      String,
  code:     String,
  folderId: Option[Long],
  userId:   Long
)

case class LinkWithClicks(
  id:       Long, 
  url:      String, 
  code:     String, 
  folderId: Option[Long], 
  userId:   Long, 
  clicks:   Int)

object LinkModel extends AppDatabase with Pagination {
  val links = Links()

  def create(user: User, url: String, code: Option[String], folderId: Option[Long]): Link = {
    (links returning links) += 
      Link(0, url, generateCode, folderId, user.id)
  }

  def click(code: String, remoteIp: String, referer: String): Option[LinkUrl] = {
    findByCode(code).map { link => 
      ClickModel.create(link, referer, remoteIp)
      LinkUrl(link.url)
    }
  }

  def forUser(user: User, offset: Option[Int], limit: Option[Int]): List[Link] = {
    links.filter(_.userId === user.id).page(limit, offset).list
  }

    

  def findByCode(code: String): Option[Link] =
    links.filter(_.code === code).firstOption

  def findByCodeWithClicks(code: String): Option[LinkWithClicks] = {
    val linksWithClicks = for { 
      (l, c) <- links leftJoin ClickModel.clicks on (_.id === _.linkId)
      if l.code === code
    } yield (l, c)

    val linkIdsWithCounters = linksWithClicks
      .groupBy(_._2.linkId)
      .map { case (l, c) => (l, c.length) }

    val linksWithCounters = for {
      (l, counts) <- links join linkIdsWithCounters on (_.id === _._1)
    } yield (l, counts)

    linksWithCounters.run match {
      case Vector((l, (_, c))) => {
        Some(LinkWithClicks(l.id, l.url, l.code, l.folderId, l.userId, c))
      }
      case Vector() => None
    }
  }

  private def getOrGenerateCode(code: Option[String]): String =
    code getOrElse generateUniqueCode

  private def generateCode: String = 
    Helper.generateRandomString(8)

  @tailrec
  private def generateUniqueCode: String = {
    val code = generateCode

    Links().filter(_.code === code).length.run match {
      case 0 => code
      case _ => generateUniqueCode
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