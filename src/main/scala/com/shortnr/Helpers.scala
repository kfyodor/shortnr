package com.shortnr

import scala.util.Random
import java.util.UUID

object Helper {
  def generateRandomString(length: Int): String = {
    (new Random).alphanumeric.take(length).map(_.toLower).mkString
  }

  def generateToken: String = {
    UUID.randomUUID().toString
  }
}