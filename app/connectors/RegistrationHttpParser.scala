/*
 * Copyright 2022 HM Revenue & Customs
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
import models.RegistrationValidationResult
import models.responses.{ConflictFound, ErrorResponse, InvalidJson, NotFound, UnexpectedResponseStatus}
import play.api.http.Status.{CONFLICT, CREATED, NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object RegistrationHttpParser extends Logging {

  type RegistrationResultResponse = Either[ErrorResponse, Unit]
  type ValidateRegistrationResponse = Either[ErrorResponse, RegistrationValidationResult]

  implicit object RegistrationResponseReads extends HttpReads[RegistrationResultResponse] {
    override def read(method: String, url: String, response: HttpResponse): RegistrationResultResponse =
      response.status match {
        case CREATED => Right(())
        case CONFLICT => Left(ConflictFound)
        case status => Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
  }

  implicit object ValidateRegistrationReads extends HttpReads[ValidateRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): ValidateRegistrationResponse = {
      response.status match {
        case OK => response.json.validate[RegistrationValidationResult] match {
          case JsSuccess(validateRegistration, _) => Right(validateRegistration)
          case JsError(errors) =>
            logger.warn(s"Failed trying to parse JSON $errors. Json was ${response.json}", errors)
            Left(InvalidJson)
        }

        case NOT_FOUND =>
          logger.warn(s"Received NotFound")
          Left(NotFound)

        case status =>
          logger.warn("Received unexpected error from return statuses")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
    }
  }

}