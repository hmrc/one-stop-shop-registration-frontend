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

package models.emails

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class EmailParametersSpec extends SpecBase {

  "RegistrationConfirmation" - {

    "serialize and deserialize correctly" in {
      val registration = RegistrationConfirmation(
        recipientName_line1 = "John Doe",
        businessName = "ABC Ltd",
        periodOfFirstReturn = "2024-04",
        firstDayOfNextPeriod = "2024-05-01",
        commencementDate = "2024-03-15",
        redirectLink = "http://example.com"
      )

      val json = Json.toJson(registration)
      val expectedJson = Json.parse(
        """
          {
            "recipientName_line1": "John Doe",
            "businessName": "ABC Ltd",
            "periodOfFirstReturn": "2024-04",
            "firstDayOfNextPeriod": "2024-05-01",
            "commencementDate": "2024-03-15",
            "redirectLink": "http://example.com"
          }
          """
      )

      json mustBe expectedJson
      json.as[RegistrationConfirmation] mustBe registration
    }

    "fail to deserialize when a required field is missing" in {
      val invalidJson = Json.parse(
        """
          {
            "businessName": "ABC Ltd",
            "periodOfFirstReturn": "2024-04",
            "firstDayOfNextPeriod": "2024-05-01",
            "commencementDate": "2024-03-15",
            "redirectLink": "http://example.com"
          }
          """
      )

      invalidJson.validate[RegistrationConfirmation] mustBe a[JsError]
    }
  }

  "AmendRegistrationConfirmation" - {

    "serialize and deserialize correctly" in {
      val amendment = AmendRegistrationConfirmation(
        recipientName_line1 = "Jane Doe",
        amendmentDate = "2024-06-01"
      )

      val json = Json.toJson(amendment)
      val expectedJson = Json.parse(
        """
          {
            "recipientName_line1": "Jane Doe",
            "amendmentDate": "2024-06-01"
          }
          """
      )

      json mustBe expectedJson
      json.as[AmendRegistrationConfirmation] mustBe amendment
    }

    "fail to deserialize when a required field is missing" in {
      val invalidJson = Json.parse(
        """
          {
            "amendmentDate": "2024-06-01"
          }
          """
      )

      invalidJson.validate[AmendRegistrationConfirmation] mustBe a[JsError]
    }
  }

  "EmailParameters" - {

    "serialize and deserialize RegistrationConfirmation correctly as EmailParameters" in {
      val registration = RegistrationConfirmation(
        recipientName_line1 = "John Doe",
        businessName = "XYZ Corp",
        periodOfFirstReturn = "2024-07",
        firstDayOfNextPeriod = "2024-08-01",
        commencementDate = "2024-06-15",
        redirectLink = "http://test.com"
      )

      val json = Json.toJson(registration)(EmailParameters.writes)
      val deserialized = json.as[RegistrationConfirmation]

      deserialized mustBe registration
    }

    "serialize and deserialize AmendRegistrationConfirmation correctly as EmailParameters" in {
      val amendment = AmendRegistrationConfirmation(
        recipientName_line1 = "Alice Smith",
        amendmentDate = "2024-09-10"
      )

      val json = Json.toJson(amendment)(EmailParameters.writes)
      val deserialized = json.as[AmendRegistrationConfirmation]

      deserialized mustBe amendment
    }

    "fail to deserialize unknown EmailParameters type" in {
      val invalidJson = Json.parse(
        """
            {
              "unknownField": "value"
            }
          """
      )

      val deserialized = invalidJson.validate[EmailParameters]

      deserialized mustBe a[JsError]
    }

    "fail to deserialize an unknown EmailParameters subtype" in {
      val json = Json.parse(
        """
          {
            "someUnknownField": "unknownValue"
          }
        """
      )

      json.validate[EmailParameters] mustBe a[JsError]
    }

    "fail to deserialize missing EmailParameters type" in {
      val invalidJson = Json.obj()

      val deserialized = invalidJson.validate[EmailParameters]

      deserialized mustBe a[JsError]
    }

    "fail to deserialize null EmailParameters type" in {
      val invalidJson = Json.parse(
        """
            {
              "businessName": null,
              "periodOfFirstReturn": "2024-04",
              "firstDayOfNextPeriod": "2024-05-01",
              "commencementDate": "2024-03-15",
              "redirectLink": "http://example.com"
            }
              """
      )

      val deserialized = invalidJson.validate[EmailParameters]

      deserialized mustBe a[JsError]
    }

    "serialize RegistrationConfirmation correctly via EmailParameters writes" in {
      val registration = RegistrationConfirmation(
        recipientName_line1 = "John Doe",
        businessName = "XYZ Corp",
        periodOfFirstReturn = "2024-07",
        firstDayOfNextPeriod = "2024-08-01",
        commencementDate = "2024-06-15",
        redirectLink = "http://test.com"
      )

      val json = Json.toJson(registration)(EmailParameters.writes)
      val expectedJson = Json.parse(
        """
          {
            "recipientName_line1": "John Doe",
            "businessName": "XYZ Corp",
            "periodOfFirstReturn": "2024-07",
            "firstDayOfNextPeriod": "2024-08-01",
            "commencementDate": "2024-06-15",
            "redirectLink": "http://test.com"
          }
        """
      )

      json mustBe expectedJson
    }

    "deserialize RegistrationConfirmation correctly via RegistrationConfirmation reads" in {
      val json = Json.parse(
        """
      {
        "recipientName_line1": "John Doe",
        "businessName": "ABC Ltd",
        "periodOfFirstReturn": "2024-04",
        "firstDayOfNextPeriod": "2024-05-01",
        "commencementDate": "2024-03-15",
        "redirectLink": "http://example.com"
      }
    """
      )

      json.validate[RegistrationConfirmation] mustBe JsSuccess(RegistrationConfirmation(
        recipientName_line1 = "John Doe",
        businessName = "ABC Ltd",
        periodOfFirstReturn = "2024-04",
        firstDayOfNextPeriod = "2024-05-01",
        commencementDate = "2024-03-15",
        redirectLink = "http://example.com"
      ))
    }

    "fail to deserialize EmailParameters from a null value" in {
      val nullJson = Json.parse("null")

      nullJson.validate[EmailParameters] mustBe a[JsError]
    }
  }
}
