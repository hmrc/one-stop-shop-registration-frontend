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
import models.previousRegistrations.NonCompliantDetails
import models.{Country, PreviousScheme}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class PreviousRegistrationSpec extends SpecBase with Matchers with ScalaCheckPropertyChecks {

  ".PreviousRegistration" - {

    "must serialise and deserialise to / from PreviousRegistrationNew" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123"
            ),
            "nonCompliantDetails" -> Json.obj(
              "nonCompliantReturns" -> 1,
              "nonCompliantPayments" -> 1
            )
          )
        )
      )

      val expectedResult: PreviousRegistration = PreviousRegistrationNew(
        country = Country("DE", "Germany"),
        previousSchemesDetails = Seq(PreviousSchemeDetails(
          previousScheme = PreviousScheme.OSSU,
          previousSchemeNumbers = PreviousSchemeNumbers("DE123", None),
          nonCompliantDetails = Some(NonCompliantDetails(
            nonCompliantReturns = Some(1),
            nonCompliantPayments = Some(1)
          ))
        )
        )
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistration] mustBe JsSuccess(expectedResult)
    }

    "must serialise and deserialise to / from PreviousRegistrationLegacy" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> "123456789"
      )

      val expectedResult: PreviousRegistration = PreviousRegistrationLegacy(
        country = Country("DE", "Germany"),
        vatNumber = "123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistration] mustBe JsSuccess(expectedResult)
    }

    "must fail deserialization for invalid structure" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "unknownField" -> "invalidData"
      )

      json.validate[PreviousRegistration] mustBe a[JsError]
    }
  }

  ".PreviousRegistrationNew" - {

    "must serialise and deserialise to / from PreviousRegistration" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123"
            ),
            "nonCompliantDetails" -> Json.obj(
              "nonCompliantReturns" -> 1,
              "nonCompliantPayments" -> 1
            )
          )
        )
      )

      val expectedResult = PreviousRegistrationNew(
        country = Country("DE", "Germany"),
        previousSchemesDetails = Seq(PreviousSchemeDetails(
          previousScheme = PreviousScheme.OSSU,
          previousSchemeNumbers = PreviousSchemeNumbers("DE123", None),
          nonCompliantDetails = Some(NonCompliantDetails(
            nonCompliantReturns = Some(1),
            nonCompliantPayments = Some(1)
          ))
        ))
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationNew] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> 12345,
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> "DE123"
            )
          )
        )
      )

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "previousSchemesDetails" -> Json.arr(
          Json.obj(
            "previousScheme" -> "ossu",
            "previousSchemeNumbers" -> Json.obj(
              "previousSchemeNumber" -> JsNull
            )
          )
        )
      )

      json.validate[PreviousRegistrationNew] mustBe a[JsError]
    }
  }

  ".PreviousRegistrationLegacy" - {

    "must serialise and deserialise to / from PreviousRegistrationLegacy" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> "123456789"
      )

      val expectedResult = PreviousRegistrationLegacy(
        country = Country("DE", "Germany"),
        vatNumber = "123456789"
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousRegistrationLegacy] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> "DE",
          "name" -> "Germany"
        ),
        "vatNumber" -> 12345
      )

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "country" -> Json.obj(
          "code" -> JsNull,
          "name" -> "Germany"
        ),
        "vatNumber" -> "123456789"
      )

      json.validate[PreviousRegistrationLegacy] mustBe a[JsError]
    }
  }

  ".PreviousSchemeDetails" - {

    "must serialise and deserialise to / from PreviousSchemeDetails" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123"
        ),
        "nonCompliantDetails" -> Json.obj(
          "nonCompliantReturns" -> 1,
          "nonCompliantPayments" -> 1
        )
      )

      val expectedResult = PreviousSchemeDetails(
        previousScheme = PreviousScheme.OSSU,
        previousSchemeNumbers = PreviousSchemeNumbers("DE123", None),
        nonCompliantDetails = Some(NonCompliantDetails(
          nonCompliantReturns = Some(1),
          nonCompliantPayments = Some(1)
        ))
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[PreviousSchemeDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "previousScheme" -> 12345,
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> "DE123"
        )
      )

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "previousScheme" -> "ossu",
        "previousSchemeNumbers" -> Json.obj(
          "previousSchemeNumber" -> JsNull
        )
      )

      json.validate[PreviousSchemeDetails] mustBe a[JsError]
    }
  }

}
