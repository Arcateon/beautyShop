package akka

import akka.Actors._
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import org.json4s.Formats
import org.json4s.jackson._

import scala.concurrent.Future
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory
import utils.Cases.InputData
import scala.concurrent.duration.DurationInt
import scala.language.postfixOps
import scala.concurrent.ExecutionContext.Implicits.global


object Routes {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  implicit val timeout: Timeout = Timeout(5 seconds)
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val route: Route = {
    path("newSession") {
      (post & entity(as[String])) { body =>
        val requestJson = JsonMethods.parse(body)
        val requestBody = requestJson.extract[InputData]
        val service = requestBody.service

        val resultFuture = {
          service match {
            case "nails" =>
              (nailsActor ? requestJson).mapTo[Boolean].flatMap { ans =>
                if(ans) {
                  Future.successful("{success: true, message: new nails session}")
                } else Future.successful("{success: false, message: session is already exist}")
              }

            case "hairCut" =>
              (hairActor ? requestJson).mapTo[Boolean].flatMap { ans =>
                if (ans) {
                  Future.successful("{success: true, message: new hairCut session}")
                } else Future.successful("{success: false, message: session is already exist}")
              }

            case "brows" =>
              (browsActor ? requestJson).mapTo[Boolean].flatMap { ans =>
                if (ans) {
                  Future.successful("{success: true, message: new brows session}")
                } else Future.successful("{success: false, message: session is already exist}")
              }

            case _ =>
              logger.info("unknown service")
              Future.successful("{success: false, message: unknown service}")
          }
        }
        complete(resultFuture)
      }
    }
  }

}
