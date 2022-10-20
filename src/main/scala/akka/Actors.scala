package akka

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import com.typesafe.scalalogging.Logger
import mongo.MongoUtils
import org.json4s.{Formats, JValue}
import org.slf4j.LoggerFactory
import utils.Cases.InputData
import scala.concurrent.ExecutionContext.Implicits.global

object Actors {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
  val system: ActorSystem = ActorSystem("System")

  class NailsActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.nailSessions
        val senderActor = sender()
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone) map {result =>
          senderActor ! result
        }

      case _ =>
        logger.info("received unknown message")
        sender ! false
    }
  }
  val nailsActor: ActorRef = system.actorOf(Props[NailsActor], name = "NailsActor")

  class HairActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.hairSessions
        val senderActor = sender()
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone) map { result =>
          senderActor ! result
        }
      case _ =>
        logger.info("received unknown message")
        sender ! false
    }
  }
  val hairActor: ActorRef = system.actorOf(Props[HairActor], name = "HairActor")

  class BrowsActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.browsSessions
        val senderActor = sender()
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone) map { result =>
          senderActor ! result
        }
      case _ =>
        logger.info("received unknown message")
        sender ! false
    }
  }
  val browsActor: ActorRef = system.actorOf(Props[BrowsActor], name = "BrowsActor")

}
