/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package connectors

import logging.Logging
import models.iossRegistration.IossEtmpDisplayRegistration
import models.responses.{ConflictFound, ErrorResponse, InvalidJson, UnexpectedResponseStatus}
import play.api.http.Status.{CONFLICT, CREATED, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RegistrationHttpParser extends Logging {

  type RegistrationResultResponse = Either[ErrorResponse, Unit]
  type IossEtmpDisplayRegistrationResultResponse = Either[ErrorResponse, IossEtmpDisplayRegistration]

  implicit object RegistrationResponseReads extends HttpReads[RegistrationResultResponse] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationResultResponse =
      response.status match {
        case CREATED => Right(())
        case CONFLICT => Left(ConflictFound)
        case status => Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
  }

  implicit object IossEtmpDisplayRegistrationReads extends HttpReads[IossEtmpDisplayRegistrationResultResponse] {

    override def read(method: String, url: String, response: HttpResponse): IossEtmpDisplayRegistrationResultResponse =
      response.status match {
        case OK => (response.json \ "registration").validate[IossEtmpDisplayRegistration] match {
          case JsSuccess(etmpDisplayRegistration, _) => Right(etmpDisplayRegistration)
          case JsError(errors) =>
            logger.error(s"Failed trying to parse IOSS Etmp Display Registration response JSON with body ${response.body}" +
              s" and status ${response.status} with errors: $errors")
            Left(InvalidJson)
        }

        case status =>
          logger.error(s"Unknown error occurred on IOSS Etmp Display Registration $status with body ${response.body}")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected IOSS registration response, status $status returned"))
      }
  }
}

object AmendRegistrationHttpParser extends Logging {

  type AmendRegistrationResultResponse = Either[ErrorResponse, Any]

  implicit object AmendRegistrationResultResponseReads extends HttpReads[AmendRegistrationResultResponse] {
    override def read(method: String, url: String, response: HttpResponse): AmendRegistrationResultResponse =
      response.status match {
        case OK => Right(())
        case status => Left(UnexpectedResponseStatus(response.status, s"Unexpected amend response, status $status returned"))
      }
  }
}