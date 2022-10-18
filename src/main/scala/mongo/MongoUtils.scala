package mongo

import com.typesafe.config._
import org.mongodb.scala.MongoClient.DEFAULT_CODEC_REGISTRY
import org.mongodb.scala.bson.codecs.Macros
import org.mongodb.scala.{ConnectionString, MongoClient, MongoClientSettings, MongoCollection, MongoDatabase}
import com.mongodb.{ServerApi, ServerApiVersion}
import com.typesafe.scalalogging.Logger
import org.bson.codecs.configuration.CodecRegistries.{fromProviders, fromRegistries}
import org.bson.codecs.configuration.{CodecProvider, CodecRegistry}
import org.json4s.Formats
import org.json4s.jackson.Serialization
import org.mongodb.scala.model.Filters._
import org.slf4j.LoggerFactory
import utils.Cases.InputData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future


object MongoUtils {

  implicit val formats: Formats = org.json4s.DefaultFormats
    .withLong.withDouble.withStrictOptionParsing

  val conf: Config = ConfigFactory.
    load("application.conf").
    getConfig("mongoConf")

  //--------------------
  //
  // Connection to mongo
  //
  //--------------------

  private val logger = Logger(LoggerFactory.getLogger(this.getClass))
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

  private val sessions: MongoCollection[InputData] = dataBase
    .withCodecRegistry(codecRegistry)
    .getCollection[InputData]("sessions")

  //---------------------
  //
  // Methods
  //
  //---------------------

  def insertNewSession(name: String, service: String, date: String,
                       time: String, phone: String): Future[Any] = {

    val document = InputData(name, service, phone, date: String, time: String)
    sessions.insertOne(document).toFuture()
  }

}
