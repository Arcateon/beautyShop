package akka

import akka.actor.Actor
import com.typesafe.scalalogging.Logger
import mongo.MongoUtils
import org.json4s.{Formats, JValue}
import org.slf4j.LoggerFactory
import utils.Cases.InputData

object Actors {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  class NailsActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.nailSessions
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone)
      case _ => logger.info("received unknown message")
    }
  }

  class HairActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.hairSessions
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone)
      case _ => logger.info("received unknown message")
    }
  }

  class BrowsActor extends Actor {

    def receive: Receive = {
      case body: JValue =>
        val document = body.extract[InputData]
        val collection = MongoUtils.browsSessions
        MongoUtils.insertNewSession(collection, document.name, document.service,
          document.date, document.time, document.phone)
      case _ => logger.info("received unknown message")
    }
  }

}
