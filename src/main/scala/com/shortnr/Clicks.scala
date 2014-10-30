package com.shortnr.tables

import scala.slick.driver.PostgresDriver.simple._

import com.shortnr.AppDatabase
import com.shortnr.serialization._
import com.shortnr.tables._

case class Click(id: Long, referer: String, remoteIp: String, linkId: Long)

object ClickModel extends AppDatabase {
  val clicks = Clicks()

  def create(link: Link, referer: String, remoteIp: String): Click =
    (clicks returning clicks) += 
      Click(0, referer, remoteIp, link.id)
}

class Clicks(tag: Tag) extends Table[Click](tag, "clicks") {
  def id       = column[Long]("id", O.PrimaryKey, O.AutoInc)
  def referer  = column[String]("referer")
  def remoteIp = column[String]("remote_ip")
  def linkId   = column[Long]("link_id")

  def link     = foreignKey("LINK_FK", linkId, Links())(_.id, onUpdate=ForeignKeyAction.Restrict, onDelete=ForeignKeyAction.Cascade)
  def linkIdx  = index("link_idx", linkId)

  def * = (id, referer, remoteIp, linkId) <> (Click.tupled, Click.unapply _)
}

object Clicks {
  def apply() = TableQuery[Clicks]
}