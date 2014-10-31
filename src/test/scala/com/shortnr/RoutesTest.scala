package com.shortnr

import spray.testkit.Specs2RouteTest

import spray.http._
import spray.util._
import spray.json._

import spray.httpx.SprayJsonSupport
import spray.httpx.SprayJsonSupport._

import org.specs2.mutable.{ Specification, Before, After }
import StatusCodes._

import com.shortnr.serialization.ShortnrJsonProtocol
import com.shortnr.serialization.ShortnrJsonProtocol._

import com.shortnr.tables._
import scala.slick.driver.PostgresDriver.simple._
import com.shortnr.tests._

class RoutesSpec extends Specification with AppDatabase
                                       with Specs2RouteTest 
                                       with ShortnrService 
                                       with BeforeAllAfterAll {

  def beforeAll = {
    createTables

    UserModel.create(1, "secret")
    UserModel.create(2, "secret")

    Folders() ++= Seq(
      Folder(0, 1, "my folder"),
      Folder(0, 1, "my folder2"),
      Folder(0, 2, "hey")
    )

    Links() ++= Seq(
      Link(0, "http://google.com", "code1", None, 1),
      Link(0, "http://google.com/a", "code2", Some(1), 1),
      Link(0, "http://google.com/b", "code3", Some(1), 1),
      Link(0, "http://google.com/c", "code4", Some(3), 2)
    )
  }

  def afterAll = dropTables

  lazy val token = {
    Users().filter(_.id === 1.toLong).map(_.token).first
  }

  def actorRefFactory = system

  "Shortnr API" should {
    "GET /" in {
      Get() ~> ShortnrRoute ~> check {
        handled must beFalse
      }
    }

    "POST /token" in {
      Post("/token", FormData(Seq("user_id" -> "1", "secret" -> "secret"))) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[UserToken] === UserToken(token)
      }
    }

    "POST /link/:code" in {
      Post("/link/code1", FormData(Seq("remote_ip" -> "1.1.1.1", "referer" -> "http://localhost"))) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[LinkUrl] === LinkUrl("http://google.com")
      }
    }

    "GET /link/:code" in {
      Get("/link/code1", Map("token" -> token)) ~> sealRoute(ShortnrRoute) ~> check {

        responseAs[LinkWithClicks] === LinkWithClicks(1, "http://google.com", "code1", None, 1, 1)
      }
    }

    "GET /link" in {
      Get("/link", Map("token" -> token)) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Link]].map(_.id) === List(1,2,3).map(_.toLong)
      }
    }

    "POST /link" in {
      Post("/link", FormData(Map("token" -> token, "url" -> "http://example.com"))) ~> sealRoute(ShortnrRoute) ~> check {
        val r = responseAs[Link]

        r.url === "http://example.com"
        r.userId === 1.toLong
      }

      Post("/link", FormData(Map("token" -> token, "code" -> "mycode", "url" -> "http://example.com"))) ~> sealRoute(ShortnrRoute) ~> check {
        val r = responseAs[Link]

        r.url === "http://example.com"
        r.code === "mycode"
        r.userId === 1.toLong
      }

      Post("/link", FormData(Map("token" -> token, "folder_id" -> "2", "url" -> "http://example.com"))) ~> sealRoute(ShortnrRoute) ~> check {
        val r = responseAs[Link]

        r.url === "http://example.com"
        r.folderId === Some(2.toLong)
        r.userId === 1.toLong
      }
    }

    "GET /folder" in {
      Get("/folder", Map("token" -> token)) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === List(1, 2).map(_.toLong)
      }

      Get("/folder", Map("token" -> token, "limit" -> "1")) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === List(1).map(_.toLong)
      }

      Get("/folder", Map("token" -> token, "limit" -> "1", "offset" -> "1")) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === List(2).map(_.toLong)
      }
    }

    "POST /folder" in {
      Post("/folder", FormData(Map("token" -> token, "name" -> "new folder"))) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[Folder] === Folder(4.toLong, 1.toLong, "new folder")
      }
    }

    "GET /folder/:id" in {
      Get("folder/1", Map("token" -> token)) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Link]].map(_.id) === List(2,3).map(_.toLong)
      }
    }
  }
}