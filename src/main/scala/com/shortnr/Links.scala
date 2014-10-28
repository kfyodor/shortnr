package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

case class Link(url: String, code: String, folderId: Long, userId: Long)

class Links(tag: Tag) extends Table[Link](tag, "links") {
  def url      = column[String]("URL")
  def code     = column[String]("CODE")
  def folderId = column[Long]("FOLDER_ID")
  def userId   = column[Long]("USER_ID")
  
  def codeInx = index("code_idx", code, unique = true)

  def * = (url, code, folderId, userId) <> (Link.tupled, Link.unapply _)
}

object Links {
  def apply() = TableQuery[Links]
}