package domain.utils

import models.Info
import org.apache.spark.rdd.RDD
import play.api.libs.json.Json
import domain.utils.Implicits._

class CustomFunctions(rdd : RDD[Info]) {
  def save() = {
    rdd.map(i => Json.toJson(i).toString ).saveAsTextFile("/home/training/so-123")
  }

}
