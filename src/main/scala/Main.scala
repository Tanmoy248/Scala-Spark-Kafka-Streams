import config.AppConfig
import dao.{MongoDao, StreamDao}
import org.apache.hadoop.security.UserGroupInformation

object Main extends App{
  println("Starting the App...")

  val topics = List(AppConfig.kafkaTopic)
  val groupId = AppConfig.kafkaGroupId
  val bootstrapServers = AppConfig.kafkaBrokers
  //UserGroupInformation.setLoginUser(UserGroupInformation.createRemoteUser("training"))
  val stream = StreamDao.start(topics,groupId, bootstrapServers)
  //val appConfig = AppConfig
  val mongo = MongoDao.getTopicOffset()
  println(mongo)
  println("finished spark job...now printing")

}
