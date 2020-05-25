package dao

import com.google.inject.Inject
import com.mongodb.casbah.Imports._
import config.AppConfig
import javax.inject.Singleton
import models.{Reports, TopicOffset}
import play.api.libs.json.{JsNumber, Json}

import collection.JavaConverters._
import scala.collection.mutable.Map
import org.bson.Document
import com.mongodb.client.model.Filters.eq
import com.mongodb.client.model.Updates._


@Singleton
class MongoDao @Inject() (appConfig: AppConfig) {
  val mongoClient = MongoClient(appConfig.mongoHost, appConfig.mongoPort)
  val db = mongoClient.getDB(appConfig.mongoDbName)

  def setup() = {
//    d(appConfig.mongoDbName).
  }

  def getTopicOffset():List[TopicOffset] = {
    val result = db.getCollection(appConfig.kafkaMetaInfo).find().toArray
    if (result.size() > 0) {
      result.asScala.toList.map(
        each => {
          val jsonResult = Json.parse(each.toString)
          println(jsonResult)
          TopicOffset(topic = appConfig.kafkaTopic,
            partition = Integer.parseInt((jsonResult \ "partition").getOrElse(JsNumber(0)).toString),
            offset = Integer.parseInt((jsonResult \ "offset").getOrElse(JsNumber(0)).toString).toLong
          )
        }
      )
    }
    else{
      List(TopicOffset(
        topic=appConfig.kafkaTopic,
        partition=0,
        offset=0
      ))
    }
  }

  def saveOffset(topicOffset: TopicOffset) = {
    val metaCollection = db.getCollection(appConfig.kafkaMetaInfo)
    val searchQuery = new BasicDBObject("topic", topicOffset.topic)
      .append("partition", topicOffset.partition)
    val newValue = new BasicDBObject("offset", topicOffset.offset)
    val updateOps = new BasicDBObject("$set", newValue)

    metaCollection.update(searchQuery, updateOps,true, false).wasAcknowledged()

  }

  def saveTimeReport(report : Reports) = {
    val reportCollection = db.getCollection(appConfig.reportSummary)
    val searchQuery = new BasicDBObject("timeBucket", report.timeBucket)
    val newValue = new BasicDBObject("driversAvailableCount", report.driversAvailableCount)
        .append("driversOnlineCount", report.driversOnlineCount)
    val updateOps = new BasicDBObject("$set", newValue)

    // add the updated element with the new values
    reportCollection.update(searchQuery,updateOps,true,false).wasAcknowledged()
  }

  def getSingleDocument(timeBucket : String) : Option[String] = {
    val filter = new BasicDBObject("timeBucket", timeBucket)
    db.getCollection(appConfig.reportSummary)
      .find(filter).toArray().asScala
      .toList.headOption.map(_.toString)
  }

}

object MongoDao extends MongoDao(AppConfig){

}
