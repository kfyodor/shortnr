package com.shortnr

import spray.json.DefaultJsonProtocol

import com.shortnr.serialization.ShortnrJsonProtocol
import ShortnrJsonProtocol._

import spray.testkit.Specs2RouteTest

import spray.util._
import spray.json._

import spray.http._

import HttpCharsets._
import MediaTypes._
import ContentTypes._

import spray.httpx.SprayJsonSupport._
import spray.httpx.unmarshalling._
import spray.httpx.marshalling._
import spray.util._

import scala.slick.driver.PostgresDriver.simple._

import org.specs2.mutable.{ Specification, Before, After }

import com.shortnr.tables._
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
        responseAs[String] === "<h1>Link shortener API</h1>"
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
      Get(s"/link/code1?token=${token}") ~> sealRoute(ShortnrRoute) ~> check {

        responseAs[LinkWithClicks] === LinkWithClicks(1, "http://google.com", "code1", None, 1, 1)
      }
    }

    "GET /link" in {
      Get(s"/link?token=${token}") ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Link]].map(_.id) === Links().filter(_.userId === 1.toLong).map(_.id).list
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
      Get(s"/folder?token=${token}") ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === Folders().filter(_.userId === 1.toLong).map(_.id).list
      }

      Get(s"/folder?token=${token}&limit=1") ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === Folders().filter(_.userId === 1.toLong).map(_.id).take(1).list
      }

      Get(s"/folder?token=${token}&limit=1&offset=1") ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Folder]].map(_.id) === Folders().filter(_.userId === 1.toLong).map(_.id).take(1).drop(1).list
      }
    }

    "POST /folder" in {
      Post("/folder", FormData(Map("token" -> token, "name" -> "new folder"))) ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[Folder] === Folder(4.toLong, 1.toLong, "new folder")
      }
    }

    "GET /folder/:id" in {
      Get(s"/folder/1?token=${token}") ~> sealRoute(ShortnrRoute) ~> check {
        responseAs[List[Link]].map(_.id) === Links().filter(_.folderId === 1.toLong).map(_.id).list
      }
    }
  }
}