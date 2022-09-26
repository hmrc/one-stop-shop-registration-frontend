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
import models.ValidateEmailResponse
import models.responses.{ErrorResponse, InvalidJson, UnexpectedResponseStatus}
import play.api.http.Status.CREATED
import play.api.libs.json.{JsError, JsSuccess}
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

object ValidateEmailHttpParser extends Logging {

  type ReturnValidateEmailResponse = Either[ErrorResponse, ValidateEmailResponse]

  implicit object ReturnValidateEmailReads extends HttpReads[ReturnValidateEmailResponse] {

    override def read(method: String, url: String, response: HttpResponse): ReturnValidateEmailResponse = {
      response.status match {
        case CREATED =>
          response.json.validate[ValidateEmailResponse] match {
            case JsSuccess(r, _) => Right(r)
            case JsError(errors) =>
              logger.error(s"ValidateEmailResponse: ${response.json}, failed to parse with errors: $errors.")
              Left(InvalidJson)
          }
        case status =>
          logger.error(s"ValidateEmailResponse received unexpected error with status: ${response.status}")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
    }
  }

}
