package com.shortnr.helpers

import scala.util.Random
import java.util.UUID

import scala.slick.driver.PostgresDriver.simple._

object Helper {
  def generateRandomString(length: Int): String = {
    (new Random).alphanumeric.take(length).map(_.toLower).mkString
  }

  def generateToken: String = {
    UUID.randomUUID().toString
  }
}

trait Pagination {
  implicit class QueryExtention[E, U, C[T]](val q: Query[E, U, C]) {
    val defaultOffset = 0
    val defaultLimit  = 10

    def page(limit: Option[Int], offset: Option[Int]) = {
      val l = limit.filter(_ > 0) getOrElse defaultLimit
      val o = offset.filter(_ > 0) getOrElse defaultOffset

      q.take(l).drop(o)
    }
  }
}