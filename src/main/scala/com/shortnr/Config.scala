package com.shortnr

import com.typesafe.config.ConfigFactory

object Config {
  val c = ConfigFactory.load()

  val dbHost = c.getString("postgres.host")
  val dbPort = c.getInt("postgres.port")

  val dbName = c.getString("postgres.dbname")
  val dbUser = c.getString("postgres.user")
  val dbPassword = c.getString("postgres.password")
}