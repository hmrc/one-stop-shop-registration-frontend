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

package models.domain.returns

import generators.Generators
import models.domain.returns.VatRateType.{Reduced, Standard}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsSuccess, Json}


class VatRateSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "VAT Rate" - {

    "must deserialise when the rate is a JsNumber" in {

      val json = Json.obj(
        "rate" -> 1.0,
        "rateType" -> Standard.toString,
        "validFrom" -> "2021-07-01"
      )

      val expectedVatRate = VatRate(BigDecimal(1.0), Standard)
      json.validate[VatRate] mustEqual JsSuccess(expectedVatRate)
    }

    "must deserialise when the rate is a JsString" in {

      val json = Json.obj(
        "rate" -> "1.0",
        "rateType" -> Standard.toString,
        "validFrom" -> "2021-07-01"
      )

      val expectedVatRate = VatRate(BigDecimal(1.0), Standard)
      json.validate[VatRate] mustEqual JsSuccess(expectedVatRate)
    }

    "must serialise and deserialise when validUntil is present" in {

      val vatRate =  VatRate(BigDecimal(1.0), Standard)

      Json.toJson(vatRate).validate[VatRate] mustEqual JsSuccess(vatRate)
    }

    "must correctly format rateForDisplay for whole numbers and decimals" in {
      VatRate(BigDecimal(20), Standard).rateForDisplay mustBe "20%"
      VatRate(BigDecimal(20.0), Standard).rateForDisplay mustBe "20%"
      VatRate(BigDecimal(20.5), Standard).rateForDisplay mustBe "20.5%"
      VatRate(BigDecimal(0), Standard).rateForDisplay mustBe "0%"
      VatRate(BigDecimal(0.75), Standard).rateForDisplay mustBe "0.75%"
      VatRate(BigDecimal(100.99), Standard).rateForDisplay mustBe "100.99%"
    }

    "must correctly serialise and deserialise a Reduced VAT rate" in {
      val vatRate = VatRate(BigDecimal(5), Reduced)
      Json.toJson(vatRate).validate[VatRate] mustEqual JsSuccess(vatRate)
    }

    "must fail deserialisation when rate is missing" in {
      val json = Json.obj("rateType" -> Standard.toString)
      json.validate[VatRate] mustBe a[JsError]
    }

    "must fail deserialisation when rateType is missing" in {
      val json = Json.obj("rate" -> 5)
      json.validate[VatRate] mustBe a[JsError]
    }

    "must fail deserialisation when rateType is invalid" in {
      val json = Json.obj("rate" -> 5, "rateType" -> "UNKNOWN")
      json.validate[VatRate] mustBe a[JsError]
    }
  }
}
