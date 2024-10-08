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
import models.responses._
import play.api.http.Status.{CONFLICT, CREATED, NOT_FOUND, OK}
import play.api.libs.json._
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.{HttpReads, HttpResponse}

import java.time.Instant


object SaveForLaterHttpParser extends Logging {

  type SaveForLaterResponse = Either[ErrorResponse, Option[SavedUserAnswers]]
  type DeleteSaveForLaterResponse = Either[ErrorResponse, Boolean]

  implicit object SaveForLaterReads extends HttpReads[SaveForLaterResponse] {
    override def read(method: String, url: String, response: HttpResponse): SaveForLaterResponse = {
      response.status match {
        case OK | CREATED =>
          response.json.validate[SavedUserAnswers] match {
            case JsSuccess(answers, _) => Right(Some(answers))
            case JsError(errors) =>
              logger.warn(s"Failed trying to parse JSON $errors. Json was ${response.json}", errors)
              Left(InvalidJson)
          }
        case NOT_FOUND =>
          logger.warn("Received NotFound for saved user answers")
          Right(None)
        case CONFLICT =>
          logger.warn("Received Conflict found for saved user answers")
          Left(ConflictFound)
        case status   =>
          logger.warn("Received unexpected error from saved user answers")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
    }
  }

  implicit object DeleteSaveForLaterReads extends HttpReads[DeleteSaveForLaterResponse] {
    override def read(method: String, url: String, response: HttpResponse): DeleteSaveForLaterResponse = {
      response.status match {
        case OK =>
          response.json.validate[Boolean] match {
            case JsSuccess(deleted, _) => Right(deleted)
            case JsError(errors) =>
              logger.warn(s"Failed trying to parse JSON $errors. Json was ${response.json}", errors)
              Left(InvalidJson)
          }
        case NOT_FOUND =>
          logger.warn("Received NotFound for saved user answers")
          Left(NotFound)
        case CONFLICT =>
          logger.warn("Received Conflict found for saved user answers")
          Left(ConflictFound)
        case status   =>
          logger.warn("Received unexpected error from saved user answers")
          Left(UnexpectedResponseStatus(response.status, s"Unexpected response, status $status returned"))
      }
    }
  }
}

case class SavedUserAnswers(
                             vrn: Vrn,
                             data: JsObject,
                             lastUpdated: Instant
                           )

object SavedUserAnswers {

  implicit val format: OFormat[SavedUserAnswers] = Json.format[SavedUserAnswers]
}
