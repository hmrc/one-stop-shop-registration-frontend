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

package models.domain

import base.SpecBase
import models.DesAddress
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.LocalDate

class VatDetailsSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks {

  "must serialise and deserialise to / from AdminUse" in {

    val json = Json.obj(
      "registrationDate" -> LocalDate.now(stubClockAtArbitraryDate),
      "address" -> Json.obj(
        "line1" -> "Line 1",
        "countryCode" -> "GB",
        "postCode" -> "AA11 1AA"
      ),
      "partOfVatGroup" -> false,
      "source" -> "etmp"
    )

    val expectedResult = VatDetails(
      registrationDate = LocalDate.now(stubClockAtArbitraryDate),
      address = DesAddress("Line 1", None, None, None, None, Some("AA11 1AA"), "GB"),
      partOfVatGroup = false,
      source = VatDetailSource.Etmp
    )

    Json.toJson(expectedResult) mustBe json
    json.validate[VatDetails] mustBe JsSuccess(expectedResult)
  }

  "must handle missing fields during deserialization" in {

    val json = Json.obj()

    json.validate[VatDetails] mustBe a[JsError]
  }

  "must handle invalid data during deserialization" in {

    val json = Json.obj(
      "registrationDate" -> LocalDate.now(stubClockAtArbitraryDate),
      "address" -> Json.obj(
        "line1" -> "Line 1",
        "countryCode" -> "GB",
        "postCode" -> "AA11 1AA"
      ),
      "partOfVatGroup" -> false,
      "source" -> 12345
    )

    json.validate[VatDetails] mustBe a[JsError]
  }

  "must handle null data during deserialization" in {

    val json = Json.obj(
      "registrationDate" -> JsNull,
      "address" -> Json.obj(
        "line1" -> "Line 1",
        "countryCode" -> "GB",
        "postCode" -> "AA11 1AA"
      ),
      "partOfVatGroup" -> false,
      "source" -> "etmp"
    )

    json.validate[VatDetails] mustBe a[JsError]
  }
}
