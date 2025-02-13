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

package models.emailVerfication

import base.SpecBase
import models.emailVerification.{EmailStatus, VerificationStatus}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}


class VerificationStatusSpec extends AnyFreeSpec with Matchers with SpecBase {

  "VerificationStatus" - {

    "must serialise and deserialise to and from a VerificationStatus" - {

      "with all optional fields present" in {

        val verificationStatus: VerificationStatus =
          VerificationStatus(
            Seq(EmailStatus(
              "email@example.com",
              true,
              false
            ))
          )

        val expectedJson = Json.obj(
          "emails" -> Json.arr(
            Json.obj(
              "emailAddress" -> "email@example.com",
              "verified" -> true,
              "locked" -> false
            )),
        )

        Json.toJson(verificationStatus) mustEqual expectedJson
        expectedJson.validate[VerificationStatus] mustEqual JsSuccess(verificationStatus)
      }

    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[VerificationStatus] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "emails" -> Json.arr(
          Json.obj(
            "emailAddress" -> 12345,
            "verified" -> true,
            "locked" -> false
          )),
      )

      json.validate[VerificationStatus] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {
      val json = Json.obj(
        "emails" -> Json.arr(
          Json.obj(
            "emailAddress" -> "email@example.com",
            "verified" -> JsNull,
            "locked" -> false
          )),
      )

      json.validate[VerificationStatus] mustBe a[JsError]
    }
  }

  "EmailStatus" - {

    "must serialise and deserialise to and from a EmailStatus" in {

        val emailStatus: EmailStatus =
          EmailStatus(
              "email@example.com",
              true,
              false
            )

        val expectedJson = Json.obj(
          "emailAddress" -> "email@example.com",
          "verified" -> true,
          "locked" -> false
        )

        Json.toJson(emailStatus) mustEqual expectedJson
        expectedJson.validate[EmailStatus] mustEqual JsSuccess(emailStatus)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EmailStatus] mustBe a[JsError]

    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "emailAddress" -> 12345,
        "verified" -> true,
        "locked" -> false
      )

      json.validate[EmailStatus] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "emailAddress" -> JsNull,
        "verified" -> true,
        "locked" -> false
      )

      json.validate[EmailStatus] mustBe a[JsError]
    }
  }

}
