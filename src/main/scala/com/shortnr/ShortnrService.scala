package com.shortnr

import akka.actor.Actor
import spray.routing._
import spray.http._
import com.shortnr.tables._

class ShortnrServiceActor extends Actor with ShortnrService {
  def actorRefFactory = context
  def receive = runRoute(ShortnrRoute)
}

trait ShortnrService extends HttpService {

  val ShortnrRoute = {
    path("token") {
      get {
        parameter("user_id") { userId =>
          complete(UserModel.findOrCreate(userId.toLong))
        }
      }
    } ~
    pathPrefix("link") {
      path(Segment) { code: String =>
        get {
          complete(s"link/$code")
        } ~
        post {
          complete(s"link/$code")
        }
      } ~ pathEnd {
        get {
          complete("link")
        } ~
        post {
          complete("link")
        }
      }
    } ~
    pathPrefix("folder") {
      path(IntNumber) { id: Int =>
        get {
          complete(s"folder/$id")
        }
      } ~ pathEnd {
        get {
          complete("folder")
        }
      }
    }
  }
}