package domain.reporting.impl

import com.google.inject.Inject
import dao.MongoDao
import play.api.libs.json.{JsValue, Json}

import scala.util.Try


class ReportService @Inject() (mongo: MongoDao) {
  // In the service layer, we resolve dao exceptions and return proper error messages
  // Or we can let the caller handle the error based on method  signature
  def renderReportsJson : (Boolean, String) = {
    val daoResult = mongo.getAllSummary
    val accumulator : List[String] = List.empty
    val (status,result) = recursiveTransform(daoResult, accumulator )
    (status, s"""{"result" : ${result.mkString("[\n", ",\n", "\n]")}}""")
  }

  private def recursiveTransform(input:Vector[String], accumulator: List[String]): (Boolean, List[String]) = {
    if (input.size == 0) (true,accumulator)
    else{
      val extracted = reportExtractor(input.head)
      if (extracted.isLeft) (false,List(extracted.left.get))
      else recursiveTransform(input.tail, accumulator ++ List(extracted.right.get))
    }
  }

  val errorMsg = "{\"Message\":\"Report Summary could not be served. Contact your Admin\"}"

  private def reportExtractor(summaryRow : String) : Either[String,String] = {
    val input = Try(Json.parse(summaryRow)).map(
      each => {
        val timeBucket = (each \\ "timeBucket")(0)
        val driversAvailableCount = (each \\ "driversAvailableCount")(0)
        val driversOnlineCount = (each \\ "driversOnlineCount")(0)
        s"""{"time" : ${timeBucket}, "available" : $driversAvailableCount , "online" : $driversOnlineCount}"""
      }
    ).toEither.fold(left => Left(errorMsg), right => Right(right))
    input
  }

}
