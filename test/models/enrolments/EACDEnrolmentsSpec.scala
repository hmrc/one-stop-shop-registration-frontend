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

package models.enrolments

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class EACDEnrolmentsSpec extends SpecBase {

  private val dateTimeEACDFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS")
  private val dateTime = "2023-12-12 15:30:45.123"
  private val eACDEnrolment1: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    activationDate = Some(LocalDateTime.parse(dateTime, dateTimeEACDFormat))
  )

  private val eACDEnrolment2: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    activationDate = Some(LocalDateTime.parse(dateTime, dateTimeEACDFormat))
  )

  "EACDEnrolments" - {

    "must deserilaise/serialise from and to an EACDEnrolments object" - {

      "with all optional fields present" in {

        val json = Json.parse(
          s"""
            {
              "enrolments": [
                {
                  "service": "${eACDEnrolment1.service}",
                  "state": "${eACDEnrolment1.state}",
                  "activationDate": "${dateTime}",
                  "identifiers": [
                    { "key": "${eACDEnrolment1.identifiers.head.key}", "value": "${eACDEnrolment1.identifiers.head.value}" },
                    { "key": "${eACDEnrolment1.identifiers.tail.head.key}", "value": "${eACDEnrolment1.identifiers.tail.head.value}" },
                    { "key": "${eACDEnrolment1.identifiers.tail.tail.head.key}", "value": "${eACDEnrolment1.identifiers.tail.tail.head.value}" }
                  ]
                 },
                 {
                   "service": "${eACDEnrolment2.service}",
                   "state": "${eACDEnrolment2.state}",
                   "activationDate": "${dateTime}",
                   "identifiers": [
                     { "key": "${eACDEnrolment2.identifiers.head.key}", "value": "${eACDEnrolment2.identifiers.head.value}" },
                     { "key": "${eACDEnrolment2.identifiers.tail.head.key}", "value": "${eACDEnrolment2.identifiers.tail.head.value}" },
                     { "key": "${eACDEnrolment2.identifiers.tail.tail.head.key}", "value": "${eACDEnrolment2.identifiers.tail.tail.head.value}" }
                   ]
                  }
              ]
            }
          """.stripMargin)

        val expectedResult = EACDEnrolments(
          enrolments = Seq(eACDEnrolment1, eACDEnrolment2)
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EACDEnrolments] mustBe JsSuccess(expectedResult)
      }

      "with all optional fields missing" in {

        val json = Json.parse(
          s"""
          {
            "enrolments": [
              {
                "service": "${eACDEnrolment1.service}",
                "state": "${eACDEnrolment1.state}",
                "identifiers": [
                  { "key": "${eACDEnrolment1.identifiers.head.key}", "value": "${eACDEnrolment1.identifiers.head.value}" },
                  { "key": "${eACDEnrolment1.identifiers.tail.head.key}", "value": "${eACDEnrolment1.identifiers.tail.head.value}" },
                  { "key": "${eACDEnrolment1.identifiers.tail.tail.head.key}", "value": "${eACDEnrolment1.identifiers.tail.tail.head.value}" }
                ]
               },
               {
                 "service": "${eACDEnrolment2.service}",
                 "state": "${eACDEnrolment2.state}",
                 "activationDate": "${dateTime}",
                 "identifiers": [
                   { "key": "${eACDEnrolment2.identifiers.head.key}", "value": "${eACDEnrolment2.identifiers.head.value}" },
                   { "key": "${eACDEnrolment2.identifiers.tail.head.key}", "value": "${eACDEnrolment2.identifiers.tail.head.value}" },
                   { "key": "${eACDEnrolment2.identifiers.tail.tail.head.key}", "value": "${eACDEnrolment2.identifiers.tail.tail.head.value}" }
                 ]
                }
            ]
          }
        """.stripMargin)

        val expectedResult = EACDEnrolments(
          enrolments = Seq(eACDEnrolment1.copy(activationDate = None), eACDEnrolment2)
        )

        Json.toJson(expectedResult) mustBe json
        json.validate[EACDEnrolments] mustBe JsSuccess(expectedResult)
      }
    }

    "must correctly handle invalid Json" in {

      val expectedJson = Json.obj()

      expectedJson.validate[EACDEnrolments] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.arr(
        "enrolments" -> Json.obj(
          "service" -> 123456789,
          "state" -> eACDEnrolment1.state,
          "activationDate" -> eACDEnrolment1.activationDate,
          "identifiers" -> eACDEnrolment1.identifiers
        )
      )

      json.validate[EACDEnrolments] mustBe a[JsError]
    }
  }
}
