package com.shortnr

import akka.actor.Actor

import scala.concurrent.{ ExecutionContext, Future }
import scala.concurrent.ExecutionContext.Implicits.global

import spray.routing._
import spray.routing.directives.AuthMagnet
import spray.routing.authentication._
import spray.http._

import com.shortnr.tables._


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
        parameters('user_id, 'secret) { (userId, secret) =>
          complete(UserModel.findOrCreate(userId.toLong))
        }
      }
    } ~
    // headerValueByName("Access-Token") { token =>
    //   authenticate(validate(token)) { currentUser =>
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
              complete("GET link")
            } ~
            post {
              formFields('url, 'code.?, 'folder_id.as[Long].?) { (url, code, folderId) =>
                complete {
                  LinkModel.create(User(1.toLong, "heyheyhey"), url, code, folderId).toString
                }
              }
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
    //   }
    // }
  }
}