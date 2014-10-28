package com.shortnr

import akka.actor.Actor
import spray.routing._
import spray.http._

class ShortnrServiceActor extends Actor with ShortnrService {
  def actorRefFactory = context
  def receive = runRoute(ShortnrRoute)
}

trait ShortnrService extends HttpService {

  val ShortnrRoute = {
    path("token") {
      get {
        complete("token")
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