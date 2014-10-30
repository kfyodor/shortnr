package com.shortnr

import akka.actor.Actor

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

import spray.routing._
import spray.routing.directives.AuthMagnet
import spray.routing.authentication._

import spray.http._
import spray.httpx.SprayJsonSupport._

import com.shortnr.tables._
import com.shortnr.serialization.ShortnrJsonProtocol._


class ShortnrServiceActor extends Actor with ShortnrService {
  def actorRefFactory = context
  def receive = runRoute(ShortnrRoute)
}

trait ShortnrService extends HttpService {

  implicit def fromFutureAuth[T](auth: ⇒ Future[Authentication[T]])(implicit executor: ExecutionContext): AuthMagnet[T] =
    new AuthMagnet(onSuccess(auth))

  def validate(token: String): Future[Authentication[User]] = {
    UserModel.findByToken(token) match {
      case Some(user) => Future(Right(user))
      case None       => Future(Left(AuthenticationFailedRejection(AuthenticationFailedRejection.CredentialsMissing, List())))
    }
  }

  val ShortnrRoute = {
    path("token") {
      get {
        parameters('user_id.as[Long], 'secret) { (userId, secret) =>
          complete {
            UserModel.authenticateOrCreate(userId, secret)
          }
        }
      }
    } ~
    anyParams('token) { token =>
      authenticate(validate(token)) { currentUser =>
        pathPrefix("link") {
          path(Segment) { code: String =>
            get {
              complete {
                LinkModel.findByCodeWithClicks(code)
              }
            } ~
            post {
              formFields('referer, 'remote_ip) { (referer, remoteIp) =>
                complete {
                  LinkModel.click(code, referer, remoteIp)
                }
              }
            }
          } ~ pathEnd {
            get {
              complete {
                LinkModel.forUser(currentUser)
              }
            } ~
            post {
              formFields('url, 'code.?, 'folder_id.as[Long].?) { (url, code, folderId) =>
                complete {
                  LinkModel.create(currentUser, url, code, folderId)
                }
              }
            }
          }
        } ~
        pathPrefix("folder") {
          path(LongNumber) { id: Long =>
            get {
              complete {
                FolderModel.linksFor(id)
              }
            }
          } ~ pathEnd {
            post {
              formFields('name) { name =>
                complete {
                  FolderModel.create(name, currentUser.id)
                }
              }
            } ~
            get {
              complete {
                FolderModel.listForUser(currentUser.id)
              }
            }
          }
        }
      }
    }
  }
}