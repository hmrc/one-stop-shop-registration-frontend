/*
 * Copyright 2023 HM Revenue & Customs
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

package models.etmp

import base.SpecBase
import models.exclusions.ExclusionReason
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import play.api.libs.json.{JsError, JsSuccess, Json}

import java.time.LocalDate

class EtmpExclusionSpec extends SpecBase {

  private val exclusionReason: ExclusionReason = Gen.oneOf(ExclusionReason.values).sample.value
  private val effectiveDate: LocalDate = LocalDate.of(2023, 2, 1)
  private val decisionDate: LocalDate = LocalDate.of(2023, 1, 15)
  private val quarantine: Boolean = arbitrary[Boolean].sample.value

  "EtmpExclusion" - {

    "must serialise/deserialise to and from EtmpExclusion" in {

      val etmpExclusion: EtmpExclusion = EtmpExclusion(
        exclusionReason = exclusionReason,
        effectiveDate = effectiveDate,
        decisionDate = decisionDate,
        quarantine = quarantine
      )

      val expectedJson = Json.obj(
        "exclusionReason" -> exclusionReason,
        "effectiveDate" -> effectiveDate,
        "decisionDate" -> decisionDate,
        "quarantine" -> quarantine
      )

      Json.toJson(etmpExclusion) mustBe expectedJson
      expectedJson.validate[EtmpExclusion] mustBe JsSuccess(etmpExclusion)
    }

    "must handle missing fields during deserialization" in {
      val expectedJson = Json.obj()

      expectedJson.validate[EtmpExclusion] mustBe a[JsError]
    }

    "must handle invalid data during deserialization" in {

      val expectedJson = Json.obj(
        "exclusionReason" -> 12345,
        "effectiveDate" -> effectiveDate,
        "decisionDate" -> decisionDate,
        "quarantine" -> quarantine
      )

      expectedJson.validate[EtmpExclusion] mustBe a[JsError]
    }
  }
}
