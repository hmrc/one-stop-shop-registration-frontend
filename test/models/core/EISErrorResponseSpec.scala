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

package models.core

import base.SpecBase
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsSuccess, Json}

import java.time.Instant
import java.util.UUID

class EISErrorResponseSpec extends AnyFreeSpec with Matchers with SpecBase {

  private val randomTimestamp = Instant.now()
  private val randomUUID = UUID.randomUUID()

  "EISErrorResponse" - {

    "must serialise and deserialise to and from a EISErrorResponse" - {

      "with all optional fields present" in {

        val eISErrorResponse: EISErrorResponse =
          EISErrorResponse(
            errorDetail = ErrorDetail(
              errorCode = Some("400"),
              errorMessage = Some("Invalid message : BEFORE TRANSFORMATION"),
              source = Some("EIS"),
              timestamp = randomTimestamp,
              correlationId = randomUUID
            )
          )

        val expectedJson = Json.obj(
          "errorDetail" -> Json.obj(
            "errorCode" -> "400",
            "errorMessage" -> "Invalid message : BEFORE TRANSFORMATION",
            "source" -> "EIS",
            "timestamp" -> randomTimestamp,
            "correlationId" -> randomUUID
          )
        )

        Json.toJson(eISErrorResponse) mustEqual expectedJson
        expectedJson.validate[EISErrorResponse] mustEqual JsSuccess(eISErrorResponse)
      }

      "with all optional fields missing" in {
        val eISErrorResponse: EISErrorResponse =
          EISErrorResponse(
            errorDetail = ErrorDetail(
              errorCode = None,
              errorMessage = None,
              source = None,
              timestamp = randomTimestamp,
              correlationId = randomUUID
            )
          )

        val expectedJson = Json.obj(
          "errorDetail" -> Json.obj(
            "timestamp" -> randomTimestamp,
            "correlationId" -> randomUUID
          )
        )

        Json.toJson(eISErrorResponse) mustEqual expectedJson
        expectedJson.validate[EISErrorResponse] mustEqual JsSuccess(eISErrorResponse)
      }

    }
  }

}
