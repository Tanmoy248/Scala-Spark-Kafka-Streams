package dao

import com.google.inject.Singleton
import models.{Reports, TopicOffset}
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, HasOffsetRanges, KafkaUtils, LocationStrategies}
import org.apache.spark.streaming.{Duration, StreamingContext}
import play.api.libs.json._
import org.joda.time.DateTime

@Singleton
class StreamDao {
  //def start() : Unit = {}


}

object StreamDao extends StreamDao {
  def start(topics : List[String], groupId : String, brokers : String) :Unit = {
    val batchDuration = new Duration(60000 * 5)
    val windowDuration = new Duration(60000 * 10)
    val slidingDuration = new Duration(60000 * 5)

    val kafkaParams : Map[String,Object] = Map(
      "bootstrap.servers" -> brokers
        ,"key.deserializer"-> classOf[StringDeserializer]
    ,"value.deserializer" -> classOf[StringDeserializer]
    ,"group.id" -> groupId
    ,"auto.offset.reset"-> "earliest"
    ,"enable.auto.commit"-> "false"
    )

    val conf:SparkConf = new SparkConf().setAppName("driverTrack").setMaster("local")
    val ssc:StreamingContext = new StreamingContext(conf, batchDuration)
//    val sc:SparkContext = new SparkContext(conf)

    // TODO : Enforce exactly-once semantics. Store the offset and start reading from there
    val fromOffsets = MongoDao.getTopicOffset().map(resultSet => {
      println(s"topic : ${resultSet.topic} partitions: ${resultSet.partition} offset: ${resultSet.offset}")
      new TopicPartition(resultSet.topic, resultSet.partition) -> resultSet.offset
    }
    ).toMap

    println("Config Setup - Done. Start Stream Processing...")
    val stream: InputDStream[ConsumerRecord[String,String]] = KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferConsistent
      , ConsumerStrategies.Assign(fromOffsets.keys.toList, kafkaParams, fromOffsets))
        //, ConsumerStrategies.Subscribe(topics, kafkaParams))

    // For idempotent pipelines, refer : https://spark.apache.org/docs/latest/streaming-kafka-0-10-integration.html#kafka-itself
    // In the direct steram, spark-kafka stream will create 1 RDD for each partition of the topic.
    // Hence, we can simply save the max(offset) for a given topic and partition in mongo
    // This offset will be used in the next streaming context startup, if the app crashes

    val result =  stream.foreachRDD( each => {
      val offsetRanges = each.asInstanceOf[HasOffsetRanges].offsetRanges
      each.map(row => {
        println(s"Meta Info: partition Number: ${row.partition()} offset : ${row.offset()} |Key info : ${row.key()}")
        (TopicOffset(
          topic=row.topic(),
          partition=row.partition(),
          offset=row.offset()
        ), extractKeys(row.value())
        )
      }
      )
      .map( metaAndVal => {
        val info = metaAndVal._2
        Tuple2(info(3), (
          Integer.parseInt(info(0).toString())
          , Integer.parseInt(info(2).toString())
          , metaAndVal._1)
        )
      }).reduceByKey(reduceTuple)
      //.foreachRDD(_
        .collect().sortBy(row => row._1.toString())
        .foreach(element => {
          val timeBucket = s"${
            element._1.toString().replace("T", " ")
              .stripPrefix("\"")
              .stripSuffix("\"")
          }:00"
          //println("Time Bucket : " + timeBucket)

          // Now first check if there is an existing entry for timebucket
          // If yes, then add the current delta to existing counts
          // Else, add a new record using mongoDb connector

          MongoDao.getSingleDocument(timeBucket) match {
            case Some(found) => {
                println(s"Existing DB Record for $timeBucket : $found")
                val historical = Reports.jsonToReportSerializer(found)
                val newUpdateRecord = Reports(
                  timeBucket,
                  historical.driversOnlineCount + element._2._1,
                  historical.driversAvailableCount + element._2._2
                )
              println("saving the offset" + element._2._3)
              println(s"timestamp : ${newUpdateRecord.timeBucket}" +
                s"| total_online_drivers : ${newUpdateRecord.driversOnlineCount} | available_drivers : ${newUpdateRecord.driversAvailableCount}")
                if (MongoDao.saveTimeReport(newUpdateRecord))
                  MongoDao.saveOffset(element._2._3)
            }
            case None => {
              println(s"Existing DB Record for $timeBucket not found")
              val newUpdateRecord = Reports(
                timeBucket,
                element._2._1,
                element._2._2
              )
              if (MongoDao.saveTimeReport(newUpdateRecord))
                MongoDao.saveOffset(element._2._3)

            }
          }


        })
    })

    ssc.start()
    ssc.awaitTermination()

    ssc.stop()
  }

  // a function to reduceByKey the tuples returned ( driver_online, driver_available, TopicOffset that has max offset)
  private def reduceTuple(a:(Int,Int,TopicOffset), b:(Int, Int,TopicOffset) ) : (Int, Int, TopicOffset) = {
    (a._1 + b._1 , a._2 + b._2,
    if (a._3.topic.contentEquals(b._3.topic) && a._3.partition == b._3.partition){
      if(a._3.offset > b._3.offset) a._3 else b._3
    }
    else b._3
    )
  }

  def extractKeys(value:String): List[JsValue] ={
    val json : JsValue = Json.parse(value)
    //println(json)
    val driverid = (json \ "driver_id").getOrElse(JsString("0"))

    // compares that the driverId is numeric non-zero, so count the record as heartbeat from driver
    val driverFlag = if (driverid.toString().contentEquals("0")) driverid else JsNumber(1):JsValue

    val timestamp = (json \ "timestamp").getOrElse(Json.parse("{\"timestamp\" : \"NA\"}"))
    //println("Timestamp read : " + timestamp)
    val onDuty = (json \ "on_duty").getOrElse(Json.parse("{\"on_duty\" : \"NA\"}"))
    val timeBucket :JsValue = {
      val strTs = timestamp.toString()
      if (strTs != "NA") JsString(new DateTime(Integer.parseInt(strTs).toLong * 1000).toString().substring(0,16))
      else JsString(strTs)
    }

    println(s"driverFlag : $driverFlag, timeBucket: $timeBucket, onDuty $onDuty")
    List(driverFlag, timestamp, onDuty, timeBucket)
  }
}
