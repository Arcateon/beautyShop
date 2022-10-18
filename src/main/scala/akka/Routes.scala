package akka

import akka.Actors._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s.Formats
import org.json4s.jackson._

import scala.concurrent.Future
import com.typesafe.scalalogging._
import mongo.MongoUtils
import org.slf4j.LoggerFactory
import utils.Cases.InputData
import scala.concurrent.ExecutionContext.Implicits.global


object Routes {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val route: Route = {
    path("newSession") {
      (post & entity(as[String])) { body =>
        val requestJson = JsonMethods.parse(body)
        val requestBody = requestJson.extract[InputData]
        val service = requestBody.service

        val resultFuture = {
          MongoUtils.insertNewSession(MongoUtils.allSessions, requestBody.name, requestBody.service,
            requestBody.date, requestBody.time, requestBody.phone) flatMap {result =>
            if(result) {
              val system = ActorSystem("System")
                service match {
                  case "nails" =>
                    val nailsActor = system.actorOf(Props[NailsActor], name = "NailsActor")
                    nailsActor ! requestJson
                    Future.successful("{success: true, message: new nails session}")
                  case "hairCut" =>
                    val hairActor = system.actorOf(Props[HairActor], name = "HairActor")
                    hairActor ! requestJson
                    Future.successful("{success: true, message: new hair cut session}")
                  case "brows" =>
                    val browsActor = system.actorOf(Props[BrowsActor], name = "BrowsActor")
                    browsActor ! requestJson
                    Future.successful("{success: true, message: new brows session}")
                  case _ =>
                    logger.info("unknown service")
                    Future.successful("{success: false, message: unknown service}")
                }
            } else Future.successful("{success: false, message: session already exist}")
          }
        }
        complete(resultFuture)
      }
    }
  }

}
