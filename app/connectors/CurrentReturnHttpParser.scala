package connectors

import connectors.CurrentReturnHttpParser.logger
import logging.Logging
import models.CurrentReturns
import models.responses.{ErrorResponse, InvalidJson, UnexpectedResponseStatus}
import play.api.http.Status.{CREATED, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object CurrentReturnHttpParser extends Logging {

  type CurrentReturnsResponse = Either[ErrorResponse, CurrentReturns]

  implicit object CurrentReturnsReads extends HttpReads[CurrentReturnsResponse] {

    override def read(method: String, url: String, response: HttpResponse): CurrentReturnsResponse = {
      response.status match {
        case OK | CREATED =>
          response.json.validate[CurrentReturns] match {
            case JsSuccess(r, _) => Right(r)
            case JsError(errors) =>
              logger.error(s"CurrentReturnsResponse: ${response.json}, failed to parse with errors: $errors.")
              Left(InvalidJson)
          }
        case status =>
          logger.error(s"CurrentReturnsResponse received unexpected error with status: ${response.status}")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
    }
  }
}