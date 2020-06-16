package domain.reporting.impl

import com.google.inject.Inject
import dao.MongoDao
import domain.utils.Helper
import play.api.libs.json.{JsString, JsValue, Json}

import scala.util.Try


class ReportService @Inject() (mongo: MongoDao
                               , helper : Helper) {
  // In the service layer, we resolve dao exceptions and return proper error messages
  // Or we can let the caller handle the error based on method  signature
  def renderReportsJson(from : Option[Long] = None, to: Option[Long] = None) : (Boolean, String) = {
    val daoResult = mongo.getAllSummary
    val accumulator : List[(Long,String)] = List.empty
    val (status,result) = recursiveTransform(daoResult, accumulator )

    // TODO Check for optimization later
    // return sorted result based on time, filter if from,to is non-empty
    val sortedResult = (from, to) match {
      case (Some(f), Some(t)) => result.sorted.filter(_._1 >= f).filter(_._1 <= t).map(_._2)
      case (Some(f), None) =>  {
        println("f : " + f)
        result.sorted.filter(_._1 >= f).map(_._2)}
      case (None, Some(t)) =>  result.sorted.filter(_._1 <= t).map(_._2)
      case (None, None) => result.sorted.map(_._2)
    }

    (status, s"""${sortedResult.mkString("[",",\n", "]")}""")
  }

  private def recursiveTransform(input:Vector[String], accumulator: List[(Long,String)]): (Boolean, List[(Long,String)]) = {
    if (input.size == 0) (true,accumulator)
    else{
      val extracted = reportExtractor(input.head)
      if (extracted.isLeft) (false,List((-1L,extracted.left.get)))
      else recursiveTransform(input.tail, accumulator ++ List(extracted.right.get))
    }
  }

  val errorMsg = "{\"Message\":\"Report Summary could not be served. Contact your Admin\"}"

  /* the below function returns a sorted tuple list */
  private def reportExtractor(summaryRow : String) : Either[String,(Long,String)] = {
    val input = Try(Json.parse(summaryRow)).map(
      each => {
        val timeBucket = (each \\ "timeBucket")(0)
        //println(timeBucket)
        val driversAvailableCount = (each \\ "driversAvailableCount")(0)
        val driversOnlineCount = (each \\ "driversOnlineCount")(0)

        val sortKey = helper.parseStringToLong(timeBucket.as[String])
        (sortKey, s"""{"time" : ${timeBucket}, "available" : $driversAvailableCount , "online" : $driversOnlineCount}""")
      }
    ).toEither.fold(left => {
      left.printStackTrace()
      Left(errorMsg)
    }, right => Right(right))
    input
  }

}
