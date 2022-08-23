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
import models.core.CoreRegistrationValidationResult
import models.responses.{ErrorResponse, InvalidJson, NotFound}
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ValidateCoreRegistrationHttpParser extends Logging {

  type ValidateCoreRegistrationResponse = Either[ErrorResponse, CoreRegistrationValidationResult]

  implicit object ValidateCoreRegistrationReads extends HttpReads[ValidateCoreRegistrationResponse] {
    override def read(method: String, url: String, response: HttpResponse): ValidateCoreRegistrationResponse = {
      response.status match {
        case OK => response.json.validate[CoreRegistrationValidationResult] match {
          case JsSuccess(validateCoreRegistration, _) => Right(validateCoreRegistration)
          case JsError(errors) =>
            logger.warn(s"Failed trying to parse JSON $errors. JSON was ${response.json}", errors)
            Left(InvalidJson)
        }

        case NOT_FOUND =>
          logger.warn(s"Received NotFound")
          Left(NotFound)
      }
    }
  }

}

