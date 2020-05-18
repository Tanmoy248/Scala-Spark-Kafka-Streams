package dao

import play.api.libs.json.JsValue

object Main extends App{
  println("Starting the App...")
  val topics = List("la-test-1")
  val groupId = "test-2"
  val bootstrapServers = "kafka-python:9092"
  val stream = StreamDao.start(topics,groupId, bootstrapServers)
  println("finished spark job...now printing")

}
