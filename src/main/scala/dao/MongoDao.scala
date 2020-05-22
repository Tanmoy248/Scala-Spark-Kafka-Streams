package dao

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import config.AppConfig
import javax.inject.Singleton
import models.TopicOffset
import play.api.libs.json.{JsNumber, Json}

import collection.JavaConverters._
import scala.collection.mutable.Map

@Singleton
class MongoDao @Inject() (appConfig: AppConfig) {
  val mongoClient = MongoClient("mongo-dev", 27017)
  val db = mongoClient.getDB(appConfig.mongoDbName)

  def getRecord() = {
    val result = db.getCollection(appConfig.kafkaMetaInfo).find().toArray
    result.asScala.toList.map(
      each => {
        val jsonResult = Json.parse(each.toString)
        println(jsonResult)
        TopicOffset(topic=appConfig.kafkaTopic,
          partition= Integer.parseInt((jsonResult \ "partition").getOrElse(JsNumber(0)).toString),
         offset = Integer.parseInt((jsonResult \ "offset").getOrElse(JsNumber(0)).toString).toLong
        )
      }
    )
      //.forEach(
      //each => println(each)
    //)
  }

}

object MongoDao extends MongoDao(AppConfig){

}
