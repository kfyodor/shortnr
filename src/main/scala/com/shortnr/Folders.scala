package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

case class Folder(id: Long, userId: Long, title: String)

class Folders(tag: Tag) extends Table[Folder](tag, "folders") {
  def id     = column[Long]("ID", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("USER_ID")
  def title  = column[String]("TITLE")
  
  def userIdInx = index("user_id_idx", userId)

  def * = (id, userId, title) <> (Folder.tupled, Folder.unapply _)
}

object Folders {
  def apply() = TableQuery[Folders]
}