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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class EuTaxIdentifierSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks {

  "must serialise and deserialise to / from AdminUse" in {

    val json = Json.obj(
      "identifierType" -> "vat",
      "value" -> "123456789"
    )

    val expectedResult = EuTaxIdentifier(
      identifierType = EuTaxIdentifierType.Vat,
      value = Some("123456789")
    )

    Json.toJson(expectedResult) mustBe json
    json.validate[EuTaxIdentifier] mustBe JsSuccess(expectedResult)
  }

  "when all optional values are absent" in {

    val json = Json.obj(
      "identifierType" -> "vat"
    )

    val expectedResult = EuTaxIdentifier(
      identifierType = EuTaxIdentifierType.Vat,
      value = None
    )

    Json.toJson(expectedResult) mustBe json
    json.validate[EuTaxIdentifier] mustBe JsSuccess(expectedResult)
  }

  "must handle missing fields during deserialization" in {

    val json = Json.obj()

    json.validate[EuTaxIdentifier] mustBe a[JsError]
  }

  "must handle invalid identifierType data during deserialization" in {

    val json = Json.obj(
      "identifierType" -> 12345,
      "value" -> "123456789"
    )

    json.validate[EuTaxIdentifier] mustBe a[JsError]
  }

  "must handle invalid value data during deserialization" in {

    val json = Json.obj(
      "identifierType" -> "vat",
      "value" -> 12345
    )

    json.validate[EuTaxIdentifier] mustBe a[JsError]
  }

  "must handle null data during deserialization" in {

    val json = Json.obj(
      "identifierType" -> JsNull,
      "value" -> JsNull
    )

    json.validate[EuTaxIdentifier] mustBe a[JsError]
  }
}
