package models

import play.api.libs.json.{JsValue, Json}

case class Reports(
                  timeBucket : String,
                  driversOnlineCount : Integer,
                  driversAvailableCount : Integer
                  )

object Reports {
  def jsonToReportSerializer(input : String): Reports = {
    val toParse = Json.parse(input)
    val timeBucket = (toParse \ "timeBucket").get
    val driversOnlineCount = Integer.parseInt((toParse \ "driversOnlineCount").get.toString())
    val driversAvailableCount = Integer.parseInt((toParse \ "driversAvailableCount").get.toString())

    Reports(timeBucket.toString(),
      driversOnlineCount,
      driversAvailableCount
    )

  }
}
