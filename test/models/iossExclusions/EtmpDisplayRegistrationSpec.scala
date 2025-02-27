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

package models.iossExclusions

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class EtmpDisplayRegistrationSpec extends SpecBase {

  private val arbEtmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  "EtmpDisplayRegistration" - {

    "must serialise/deserialise from and to an EtmpDisplayRegistration" in {

      val json = Json.obj(
        "exclusions" -> arbEtmpDisplayRegistration.exclusions
      )

      val expectedResult = EtmpDisplayRegistration(
        exclusions = arbEtmpDisplayRegistration.exclusions
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpDisplayRegistration] mustBe JsSuccess(expectedResult)
    }

    "handle empty data object" in {

      val json = Json.obj()

      json.validate[EtmpDisplayRegistration] mustBe a[JsError]
    }
  }
}
