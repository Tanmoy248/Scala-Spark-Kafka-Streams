package dao

object Main extends App{
  println("Starting the App...")
  val topics = List("lalamove-test-1")
  val groupId = "test-2"
  val bootstrapServers = "kafka-python:9092"
  //val stream = StreamDao.start(topics,groupId, bootstrapServers)
  //val appConfig = AppConfig
  val mongo = MongoDao.getRecord()
  println(mongo)
  println("finished spark job...now printing")

}
