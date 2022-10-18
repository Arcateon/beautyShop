package mongo

import com.typesafe.config._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase, documentToUntypedDocument}
import com.mongodb.{ServerApi, ServerApiVersion}
import com.typesafe.scalalogging.Logger
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.json4s.Formats
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory
import utils.Cases.InputData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.reflect.Manifest.Any


object MongoUtils {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing
  private val logger = Logger(LoggerFactory.getLogger(this.getClass))

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("mongoConf")

  //--------------------
  //
  // Connection to mongo
  //
  //--------------------

  private val uri: String = conf.getString("url")
  private val db: String = conf.getString("dataBase")

  private val mongoClientSettings = MongoClientSettings.builder()
    .applyConnectionString(ConnectionString(uri))
    .serverApi(ServerApi.builder().version(ServerApiVersion.V1).build())
    .build()

  private val mongoClient = MongoClient(mongoClientSettings)
  private val dataBase: MongoDatabase = mongoClient.getDatabase(db)

  //---------------------
  //
  // Codecs
  //
  //---------------------

  private val codecProvider: CodecProvider = Macros
    .createCodecProviderIgnoreNone[InputData]()
  private val codecRegistry: CodecRegistry =
    fromRegistries(fromProviders(codecProvider)
      , DEFAULT_CODEC_REGISTRY)

  //---------------------
  //
  // Collections
  //
  //---------------------

  val allSessions: MongoCollection[InputData] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputData]("allSessions")
  val nailSessions: MongoCollection[InputData] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputData]("nailSessions")
  val hairSessions: MongoCollection[InputData] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputData]("hairSessions")
  val browsSessions: MongoCollection[InputData] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputData]("browsSessions")

  //---------------------
  //
  // Methods
  //
  //---------------------

  def insertNewSession(collection: MongoCollection[InputData], name: String, service: String,
                       date: String, time: String, phone: String): Future[Boolean] = {

    val documentsWithDateF = allSessions.find(and(equal("service", service),
      equal("date", date), equal("time", time))).toFuture

    documentsWithDateF flatMap { document =>
      if(document.isEmpty) {
        val document = InputData(name, service, phone, date: String, time: String)
        allSessions.insertOne(document).toFuture
        collection.insertOne(document).toFuture
        Future.successful(true)
      } else {
        Future.successful(false)
      }
    } recoverWith {
        case ex : Exception => logger.info(s"$ex")
        Future.successful(false)
    }

  }

}
