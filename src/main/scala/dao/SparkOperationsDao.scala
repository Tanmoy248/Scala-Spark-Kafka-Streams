package dao

import domain.utils.CustomFunctions
import models.Info
import org.apache.spark.{SparkConf, SparkContext}


class SparkOperationsDao {
  val conf:SparkConf = new SparkConf().setAppName("driverTrack").setMaster("local")
  val sc = new SparkContext(conf)

  def writeToElastic() = {
    val sample = List(Info("name1", "city1", "123"), Info("name2", "city2", "234"))
    val rdd = sc.parallelize(sample)
    val converter = new CustomFunctions(rdd)
    converter.save()
  }

}

object SparkOperationsDao extends SparkOperationsDao
