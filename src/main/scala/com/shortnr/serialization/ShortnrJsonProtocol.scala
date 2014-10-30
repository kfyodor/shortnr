package com.shortnr.serialization

import com.shortnr.tables._
import spray.json._

object ShortnrJsonProtocol extends DefaultJsonProtocol {
  implicit def userFormat   = jsonFormat2(User)
  implicit def folderFormat = jsonFormat3(Folder)
  implicit def linkFormat   = jsonFormat5(Link)
  implicit def tokenFormat  = jsonFormat1(UserToken)
}