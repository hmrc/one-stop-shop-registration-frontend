/*
 * Copyright 2025 HM Revenue & Customs
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

package models.enrolments

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EACDEnrolmentSpec extends SpecBase {


  private val dateTimeEACDFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
  private val dateTime = "2023-12-12 15:30:45.123"
  private val eACDEnrolment: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value

  "EACDEnrolment" - {

    "must deserilaise/serialise from and to an EACDEnrolment object" - {

      "with all optional fields present" in {

        val json = Json.parse(
          s"""
            {
              "service": "${eACDEnrolment.service}",
              "state": "${eACDEnrolment.state}",
              "activationDate": "${dateTime}",
              "identifiers": [
                { "key": "${eACDEnrolment.identifiers.head.key}", "value": "${eACDEnrolment.identifiers.head.value}" }
              ]
            }
          """.stripMargin)

        val expectedResult = EACDEnrolment(
          service = eACDEnrolment.service,
          state = eACDEnrolment.state,
          activationDate = Some(LocalDateTime.parse(dateTime, dateTimeEACDFormat)),
          identifiers = Seq(eACDEnrolment.identifiers.head)
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EACDEnrolment] mustBe JsSuccess(expectedResult)
      }

      "with all optional fields missing" in {

        val json = Json.obj(
          "service" -> eACDEnrolment.service,
          "state" -> eACDEnrolment.state,
          "identifiers" -> eACDEnrolment.identifiers
        )

        val expectedResult = EACDEnrolment(
          service = eACDEnrolment.service,
          state = eACDEnrolment.state,
          activationDate = None,
          identifiers = eACDEnrolment.identifiers
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EACDEnrolment] mustBe JsSuccess(expectedResult)
      }
    }

    "must correctly handle invalid Json" in {

      val json = Json.obj()

      json.validate[EACDEnrolment] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "service" -> 123456789,
        "state" -> eACDEnrolment.state,
        "activationDate" -> dateTime,
        "identifiers" -> eACDEnrolment.identifiers
      )

      json.validate[EACDEnrolment] mustBe a[JsError]
    }
  }
}
