package akka

import akka.Actors._
import akka.actor.{ActorSystem, Props}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import org.json4s.Formats
import org.json4s.jackson._
import scala.concurrent.Future
import com.typesafe.scalalogging._
import org.slf4j.LoggerFactory
import utils.Cases.InputData


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
          val system = ActorSystem("System")
          service match {
            case "nails" =>
              val nailsActor = system.actorOf(Props[NailsActor], name = "NailsActor")
              nailsActor ! requestJson
            case "hairCut" =>
              val hairActor = system.actorOf(Props[HairActor], name = "HairActor")
              hairActor ! requestJson
            case "brows" =>
              val browsActor = system.actorOf(Props[BrowsActor], name = "BrowsActor")
              browsActor ! requestJson
            case Exception => logger.info("unknown service")
          }

          Future.successful("success")
        }
        complete(resultFuture)
      }
    }
  }

}
