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

class EACDIdentifiersSpec extends SpecBase {

  private val eACDIdentifiers: EACDIdentifiers = arbitraryEACDIdentifiers.arbitrary.sample.value

  "EACDIdentifiers" - {

    "must deserilaise/serialise from and to an EACDIdentifiers object" - {

      val json = Json.parse(
        s"""
          {
            "key": "${eACDIdentifiers.key}",
            "value": "${eACDIdentifiers.value}"
          }
        """.stripMargin)

      val expectedResult = EACDIdentifiers(
        key = eACDIdentifiers.key,
        value = eACDIdentifiers.value,
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EACDIdentifiers] mustBe JsSuccess(expectedResult)
    }

    "must correctly handle invalid Json" in {

      val json = Json.obj()

      json.validate[EACDIdentifiers] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "key" -> 123456789,
        "value" -> eACDIdentifiers.value,
      )

      json.validate[EACDIdentifiers] mustBe a[JsError]
    }
  }
}
