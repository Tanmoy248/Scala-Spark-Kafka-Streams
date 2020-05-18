package dao

import com.google.inject.Singleton
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark._
import org.apache.spark.streaming.dstream.{DStream, InputDStream}
import org.apache.spark.streaming.kafka010.{ConsumerStrategies, KafkaUtils, LocationStrategies}
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

    println("Config Setup - Done. Start Stream Processing...")
    val stream: InputDStream[ConsumerRecord[String,String]] = KafkaUtils.createDirectStream(ssc, LocationStrategies.PreferConsistent
      , ConsumerStrategies.Subscribe(topics, kafkaParams))

    val result =  stream.map(each => extractKeys(each.value))
      .map(info => Tuple2(info(3), Tuple2(
        Integer.parseInt(info(0).toString())
        , Integer.parseInt(info(2).toString()))
      )).reduceByKey(reduceTuple)
    .foreachRDD(_.collect()
      .foreach(element =>{
        println(s"for the key : ${element._1.toString().replace("T"," ")}:00 " +
          s"| total_online_drivers : ${element._2._1} | available_drivers : ${element._2._2}")
      })
    )

//    val dataset:RDD[String] = sc.textFile("/home/training/interviews/lalamove/data_1.json")
//    dataset.map(each => {
//      extractKeys(each)
//    })

    ssc.start()
    ssc.awaitTermination()

    ssc.stop()
  }

  // a function to reduceByKey the tuples returned ( driver_online, driver_available)
  private def reduceTuple(a:(Int,Int), b:(Int, Int) ) = {
    (a._1 + b._1 , a._2 + b._2)
  }

  def extractKeys(value:String): List[JsValue] ={
    val json : JsValue = Json.parse(value)
    //println(json)
    val driverid = (json \ "driver_id").getOrElse(JsString("0"))
    //val tmp : JsValue = JsString("1")
    val driverFlag = if (driverid.toString().contentEquals("0")) driverid else JsNumber(1):JsValue
    val timestamp = (json \ "timestamp").getOrElse(Json.parse("{\"timestamp\" : \"NA\"}"))
    println("Timestamp read : " + timestamp)
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
