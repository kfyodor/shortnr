package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.AppDatabase
import com.shortnr.helpers.Pagination
import com.shortnr.tables._

case class Folder(id: Long, userId: Long, title: String)

object FolderModel extends AppDatabase with Pagination {
  val folders = Folders()

  def find(id: Long): Option[Folder] = {
    folders.filter(_.id === id).list match {
      case List(folder) => Some(folder)
      case _            => None
    }
  }

  def linksFor(id: Long, offset: Option[Int], limit: Option[Int]): List[Link] =
    Links().filter(_.folderId === id).page(limit, offset).list

  def listForUser(userId: Long, offset: Option[Int], limit: Option[Int]): List[Folder] =
    folders.filter(_.userId === userId).page(limit, offset).list

  def create(name: String, userId: Long): Folder =
    (folders returning folders) += Folder(0, userId, name)
}

class Folders(tag: Tag) extends Table[Folder](tag, "folders") {
  def id      = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId  = column[Long]("user_id")
  def title   = column[String]("title")

  def user    = foreignKey("user_fk", userId, Users())(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  
  def userIdx = index("user_id_idx", userId)
  def nameIdx = index("name_idx", (title, userId), unique = true)

  def * = (id, userId, title) <> (Folder.tupled, Folder.unapply _)
}

object Folders {
  def apply() = TableQuery[Folders]
}