package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.http.HttpEntity
import config.AppConfig
import domain.reporting.impl.ReportService
import domain.utils.Helper

import scala.util.{Try,Success,Failure}
import akka.util.ByteString

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, report:ReportService , helper : Helper) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def reports(timeFrom:Option[String] , timeTo: Option[String]) = Action { implicit request: Request[AnyContent] => {
    println("TRACE---> "+ timeFrom + "....." + timeTo)

    val inputValidation = Try {
      (timeFrom.map(helper.parseStringToLong(_)), timeTo.map(helper.parseStringToLong(_))
      )
    }



    // add validation of input params and then call report.renderReportsJson
    // If the Success, generate report
    // On failure, send 503 with cause
    val (status, message) = inputValidation match {
      case Success(params) => {
        println("input validation : " + params )
        report.renderReportsJson(params._1, params._2)
          //.getOrElse(report.renderReportsJson())
      } // default to None params
      case Failure(msg) => {
        msg.printStackTrace()
        (400, msg.getMessage)
      }
    }

    // Adding proper headers, send response
    // status can be true = successful (200)
    //               false = some error in report generation(503)
    //               400 = bad request , non parseable dates
    status match {
      case true => Result(
        header = ResponseHeader(200, Map("Access-Control-Allow-Origin" -> "*")),
        body = HttpEntity.Strict(ByteString(message), Some("text/plain"))
      )
      case false => Result(
        header = ResponseHeader(503, Map("Access-Control-Allow-Origin" -> "*")),
        body = HttpEntity.Strict(ByteString(message), Some("text/plain"))
      )
      case 400 => Result(header = ResponseHeader(400, Map("Access-Control-Allow-Origin" -> "*")),
        body = HttpEntity.Strict(ByteString(message), Some("text/plain"))
      )
    }
  }
  }
}