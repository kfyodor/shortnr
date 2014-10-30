package com.shortnr.serialization

import com.shortnr.tables._
import spray.json._

object ShortnrJsonProtocol extends DefaultJsonProtocol {
  implicit val userFormat   = jsonFormat2(User)
  implicit val folderFormat = jsonFormat3(Folder)
  implicit val linkFormat   = jsonFormat5(Link)
  implicit val tokenFormat  = jsonFormat1(UserToken)
}