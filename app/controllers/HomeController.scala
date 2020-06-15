package controllers

import javax.inject._
import play.api._
import play.api.mvc._
import play.api.http.HttpEntity

import config.AppConfig
import domain.reporting.impl.ReportService
import akka.util.ByteString

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents, report:ReportService) extends AbstractController(cc) {

  /**
   * Create an Action to render an HTML page.
   *
   * The configuration in the `routes` file means that this method
   * will be called when the application receives a `GET` request with
   * a path of `/`.
   */
  def index() = Action { implicit request: Request[AnyContent] => {
    val (status, message) = report.renderReportsJson

    // Adding proper headers
    if (status) {
      Result(
        header = ResponseHeader(200, Map("Access-Control-Allow-Origin" -> "*")),
        body = HttpEntity.Strict(ByteString(message), Some("text/plain"))
      )
    }
    else
      {
        Result(
          header = ResponseHeader(501, Map("Access-Control-Allow-Origin" -> "*")),
          body = HttpEntity.Strict(ByteString(message), Some("text/plain"))
        )
      }
  }
  }
}