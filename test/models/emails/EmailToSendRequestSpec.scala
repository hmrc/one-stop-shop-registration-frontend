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
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

class EmailToSendRequestSpec extends SpecBase {

  "EmailToSendRequest" - {

    "serialize correctly with RegistrationConfirmation" in {
      val emailRequest = EmailToSendRequest(
        to = List("recipient@example.com"),
        templateId = "registration-template",
        parameters = RegistrationConfirmation(
          recipientName_line1 = "John Doe",
          businessName = "XYZ Corp",
          periodOfFirstReturn = "2024-07",
          firstDayOfNextPeriod = "2024-08-01",
          commencementDate = "2024-06-15",
          redirectLink = "http://test.com"
        ),
        force = true
      )

      val expectedJson = Json.parse(
        """
            {
              "to": ["recipient@example.com"],
              "templateId": "registration-template",
              "parameters": {
                "recipientName_line1": "John Doe",
                "businessName": "XYZ Corp",
                "periodOfFirstReturn": "2024-07",
                "firstDayOfNextPeriod": "2024-08-01",
                "commencementDate": "2024-06-15",
                "redirectLink": "http://test.com"
              },
              "force": true
            }
          """
      )

      Json.toJson(emailRequest) mustBe expectedJson
    }

    "deserialize correctly with RegistrationConfirmation" in {
      val validJson = Json.parse(
        """
                {
                  "to": ["recipient@example.com"],
                  "templateId": "registration-template",
                  "parameters": {
                    "recipientName_line1": "John Doe",
                    "businessName": "XYZ Corp",
                    "periodOfFirstReturn": "2024-07",
                    "firstDayOfNextPeriod": "2024-08-01",
                    "commencementDate": "2024-06-15",
                    "redirectLink": "http://test.com"
                  },
                  "force": true
                }
              """
      )

      val expectedEmailRequest = EmailToSendRequest(
        to = List("recipient@example.com"),
        templateId = "registration-template",
        parameters = RegistrationConfirmation(
          recipientName_line1 = "John Doe",
          businessName = "XYZ Corp",
          periodOfFirstReturn = "2024-07",
          firstDayOfNextPeriod = "2024-08-01",
          commencementDate = "2024-06-15",
          redirectLink = "http://test.com"
        ),
        force = true
      )

      validJson.validate[EmailToSendRequest] mustBe JsSuccess(expectedEmailRequest)
    }

    "serialize correctly with AmendRegistrationConfirmation" in {
      val emailRequest = EmailToSendRequest(
        to = List("user@example.com"),
        templateId = "amend-template",
        parameters = AmendRegistrationConfirmation(
          recipientName_line1 = "Alice Smith",
          amendmentDate = "2024-09-10"
        ),
        force = false
      )

      val expectedJson = Json.parse(
        """
            {
              "to": ["user@example.com"],
              "templateId": "amend-template",
              "parameters": {
                "recipientName_line1": "Alice Smith",
                "amendmentDate": "2024-09-10"
              },
              "force": false
            }
          """
      )

      Json.toJson(emailRequest) mustBe expectedJson
    }

    "deserialize correctly with AmendRegistrationConfirmation" in {
      val validJson = Json.parse(
        """
                {
                  "to": ["recipient@example.com"],
                  "templateId": "registration-template",
                  "parameters": {
                    "recipientName_line1": "Alice Smith",
                    "amendmentDate": "2024-09-10"
                  },
                  "force": true
                }
              """
      )

      val expectedEmailRequest = EmailToSendRequest(
        to = List("recipient@example.com"),
        templateId = "registration-template",
        parameters = AmendRegistrationConfirmation(
          recipientName_line1 = "Alice Smith",
          amendmentDate = "2024-09-10"
        ),
        force = true
      )

      validJson.validate[EmailToSendRequest] mustBe JsSuccess(expectedEmailRequest)
    }

    "fail to deserialize when a required field is missing" in {
      val invalidJson = Json.parse(
        """
            {
              "templateId": "registration-template",
              "parameters": {
                "recipientName_line1": "John Doe",
                "businessName": "XYZ Corp",
                "periodOfFirstReturn": "2024-07",
                "firstDayOfNextPeriod": "2024-08-01",
                "commencementDate": "2024-06-15",
                "redirectLink": "http://test.com"
              },
              "force": true
            }
          """
      )

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }

    "fail to deserialize when parameters field is invalid" in {
      val invalidJson = Json.parse(
        """
            {
              "to": ["recipient@example.com"],
              "templateId": "registration-template",
              "parameters": {
                "unknownField": "invalid data"
              },
              "force": true
            }
          """
      )

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }

    "fail to deserialize when parameters field is null" in {
      val invalidJson = Json.obj(
        "to" -> JsNull,
        "templateId" -> "registration-template",
        "parameters" -> Json.obj(
          "unknownField" -> "invalid data"
        ),
        "force" -> true
      )

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }

    "fail to deserialize when an unexpected field is present" in {

      val invalidJson = Json.parse(
        """
            {
              "to": ["recipient@example.com"],
              "templateId": "registration-template",
              "parameters": {
                "unknownField": "invalid data"
              },
              "force": true,
              "extraField": "unexpected"
            }
          """
      )

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }

    "handle missing values" in {

      val invalidJson = Json.obj()

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }

    "handle empty string values" in {

      val invalidJson = Json.parse(
        """
                            {
                              "to": [""],
                              "templateId": "",
                              "parameters": {
                                "unknownField": ""
                              },
                              "force": true
                            }
                          """
      )

      invalidJson.validate[EmailToSendRequest] mustBe a[JsError]
    }
  }
}
