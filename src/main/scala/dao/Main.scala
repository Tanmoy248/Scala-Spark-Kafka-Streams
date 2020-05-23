package dao

import config.AppConfig

object Main extends App{
  println("Starting the App...")

  val topics = List(AppConfig.kafkaTopic)
  val groupId = AppConfig.kafkaGroupId
  val bootstrapServers = "kafka-python:9092"
  val stream = StreamDao.start(topics,groupId, bootstrapServers)
  //val appConfig = AppConfig
  val mongo = MongoDao.getTopicOffset()
  println(mongo)
  println("finished spark job...now printing")

}
