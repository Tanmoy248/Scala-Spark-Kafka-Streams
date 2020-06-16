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
    val query = DBObject("topic" -> appConfig.kafkaTopic)
    val result = Option(db.getCollection(appConfig.kafkaMetaInfo).find(query).toArray())
    println("TRACE-2 ----->")
    println(result.toString)

    // If result not found, reset to 0
    result.map(each => {
      // the reason for having the asScala is to iterate over the list (incase of multi partition topic)
      each.asScala.map(topicPartitions => {
        println("TRACE - 3 ---> " + topicPartitions.toString())
        val jsonResult = Json.parse(topicPartitions.toString)
        println(jsonResult)
        TopicOffset(topic = appConfig.kafkaTopic,
          partition = Integer.parseInt((jsonResult \ "partition").getOrElse(JsNumber(0)).toString),
          offset = Integer.parseInt((jsonResult \ "offset").getOrElse(JsNumber(0)).toString).toLong
        )
      }
      )
    }).getOrElse({
        List(TopicOffset(
          topic = appConfig.kafkaTopic,
          partition = 0,
          offset = 0
        ))
      }).toList
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

  def getAllSummary:Vector[String] = {
    db.getCollection(appConfig.reportSummary)
      .find().toArray().asScala.toVector.map(_.toString)
  }

}

object MongoDao extends MongoDao(AppConfig){

}


