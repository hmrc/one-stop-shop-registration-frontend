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

import generators.Generators
import models.{Country, InternationalAddress}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class EuTaxRegistrationSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "must serialise and deserialise from / to an EU VAT Registration" in {

    forAll(arbitrary[Country], arbitrary[EuTaxIdentifier], arbitrary[String], arbitrary[InternationalAddress]) {
      case (country, vatNumber, tradingName, address) =>

        val euVatRegistration = RegistrationWithoutFixedEstablishmentWithTradeDetails(country, vatNumber, TradeDetails(tradingName, address))

        val json = Json.toJson(euVatRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euVatRegistration)
    }
  }

  "must serialise and deserialise from / to a Registration with Fixed Establishment" in {

    forAll(arbitrary[Country], arbitrary[TradeDetails], arbitrary[EuTaxIdentifier]) {
      case (country, fixedEstablishment, taxRef) =>

        val euRegistration = RegistrationWithFixedEstablishment(country, taxRef, fixedEstablishment)

        val json = Json.toJson(euRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
    }
  }

  "must serialise and deserialise from / to a Registration without Fixed Establishment" in {

    forAll(arbitrary[Country]) {
      country =>
        val euRegistration = RegistrationWithoutTaxId(country)

        val json = Json.toJson(euRegistration)
        json.validate[EuTaxRegistration] mustEqual JsSuccess(euRegistration)
    }
  }

  ".EuVatRegistration" - {

    "must serialise and deserialise to / from EuVatRegistration" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> "123456789"
      )

      val expectedResult = EuVatRegistration(
        country = Country("DE", "Germany"),
        vatNumber = "123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EuVatRegistration] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> 12345
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> JsNull
      )

      json.validate[EuVatRegistration] mustBe a[JsError]
    }
  }

  ".RegistrationWithFixedEstablishment" - {

    "must serialise and deserialise to / from RegistrationWithFixedEstablishment" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> "Ireland"
            )
          )
        )
      )

      val expectedResult = RegistrationWithFixedEstablishment(
        country = Country("DE", "Germany"),
        taxIdentifier = EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
        fixedEstablishment = TradeDetails(
          "Irish trading name",
          InternationalAddress(
            line1 = "Line 1",
            line2 = None,
            townOrCity = "Town",
            stateOrRegion = None,
            None,
            Country("IE", "Ireland")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithFixedEstablishment] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> "Ireland"
            )
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "fixedEstablishment" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> JsNull
            )
          )
        )
      )

      json.validate[RegistrationWithFixedEstablishment] mustBe a[JsError]
    }
  }

  ".RegistrationWithoutFixedEstablishmentWithTradeDetails" - {

    "must serialise and deserialise to / from RegistrationWithoutFixedEstablishmentWithTradeDetails" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> "Ireland"
            )
          )
        )
      )

      val expectedResult = RegistrationWithoutFixedEstablishmentWithTradeDetails(
        country = Country("DE", "Germany"),
        taxIdentifier = EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
        tradeDetails = TradeDetails(
          "Irish trading name",
          InternationalAddress(
            line1 = "Line 1",
            line2 = None,
            townOrCity = "Town",
            stateOrRegion = None,
            None,
            Country("IE", "Ireland")
          )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> "Ireland"
            )
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "taxIdentifier" -> Json.obj(
          "identifierType" -> "vat",
          "value" -> "123456789"
        ),
        "tradeDetails" -> Json.obj(
          "tradingName" -> "Irish trading name",
          "address" -> Json.obj(
            "line1" -> "Line 1",
            "townOrCity" -> "Town",
            "country" -> Json.obj(
              "code" -> "IE",
              "name" -> JsNull
            )
          )
        )
      )

      json.validate[RegistrationWithoutFixedEstablishmentWithTradeDetails] mustBe a[JsError]
    }
  }

  ".RegistrationWithoutTaxId" - {

    "must serialise and deserialise to / from RegistrationWithoutTaxId" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        )
      )

      val expectedResult = RegistrationWithoutTaxId(
        country = Country("DE", "Germany")
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[RegistrationWithoutTaxId] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> JsNull
        )
      )

      json.validate[RegistrationWithoutTaxId] mustBe a[JsError]
    }
  }
}
