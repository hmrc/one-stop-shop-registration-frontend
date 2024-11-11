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
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsString, JsSuccess, Json}

class EtmpExclusionSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val arbEtmpExclusion: EtmpExclusion = arbitraryEtmpExclusion.arbitrary.sample.value

  "EtmpExclusion" - {

    "must serialise/deserialise from and to an EtmpExclusion" in {

      val json = Json.obj(
        "exclusionReason" -> arbEtmpExclusion.exclusionReason,
        "effectiveDate" -> arbEtmpExclusion.effectiveDate,
        "decisionDate" -> arbEtmpExclusion.decisionDate,
        "quarantine" -> arbEtmpExclusion.quarantine
      )

      val expectedResult = EtmpExclusion(
        exclusionReason = arbEtmpExclusion.exclusionReason,
        effectiveDate = arbEtmpExclusion.effectiveDate,
        decisionDate = arbEtmpExclusion.decisionDate,
        quarantine = arbEtmpExclusion.quarantine
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[EtmpExclusion] mustBe JsSuccess(expectedResult)
    }
  }

  "EtmpExclusionReason" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(EtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>
          JsString(etmpExclusionReason.toString).validate[EtmpExclusionReason].asOpt.value mustBe etmpExclusionReason
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String].suchThat(!EtmpExclusionReason.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValues =>

          JsString(invalidValues).validate[EtmpExclusionReason] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(EtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>

          Json.toJson(etmpExclusionReason) mustBe JsString(etmpExclusionReason.toString)
      }
    }
  }
}
