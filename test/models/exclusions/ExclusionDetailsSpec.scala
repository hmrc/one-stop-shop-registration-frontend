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

package models.exclusions

import base.SpecBase
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsSuccess, Json}

import java.time.LocalDate

class ExclusionDetailsSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val genExclusionReason: ExclusionReason = Gen.oneOf(ExclusionReason.values).sample.value

  "ExclusionDetails" - {

    "must deserialise/serialise to and from ExclusionDetails" in {

      val json = Json.obj(
        "exclusionRequestDate" -> LocalDate.of(2021, 10, 1),
        "exclusionReason" -> genExclusionReason
      )

      val expectedResult = ExclusionDetails(
        exclusionRequestDate = LocalDate.of(2021, 10, 1),
        exclusionReason = genExclusionReason,
        movePOBDate = None,
        issuedBy = None,
        vatNumber = None

      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ExclusionDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[ExclusionDetails] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val json = Json.obj(
        "exclusionRequestDate" -> LocalDate.of(2021, 10, 1),
        "exclusionReason" -> 1
      )

      json.validate[ExclusionDetails] mustBe a[JsError]
    }

    "must handle null data during deserialization" in {

      val json = Json.obj(
        "exclusionRequestDate" -> LocalDate.of(2021, 10, 1),
        "exclusionReason" -> JsNull
      )

      json.validate[ExclusionDetails] mustBe a[JsError]
    }
  }

}