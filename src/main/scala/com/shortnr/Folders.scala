package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

case class Folder(id: Long, userId: Long, title: String)

class Folders(tag: Tag) extends Table[Folder](tag, "folders") {
  def id     = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def userId = column[Long]("user_id")
  def title  = column[String]("title")

  def user   = foreignKey("user_fk", userId, Users())(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  
  def userIdx = index("user_id_idx", userId)

  def * = (id, userId, title) <> (Folder.tupled, Folder.unapply _)
}

object Folders {
  def apply() = TableQuery[Folders]
}