import dao.SparkOperationsDao

object RapidTests extends App {
  SparkOperationsDao.writeToElastic()
    //.collect.foreach(println)

}
