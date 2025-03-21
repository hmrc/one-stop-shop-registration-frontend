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

package models.iossRegistration

import base.SpecBase
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.*

class IossEtmpExclusionSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val arbEtmpExclusion: IossEtmpExclusion = arbitraryIossEtmpExclusion.arbitrary.sample.value

  "IossEtmpExclusion" - {

    "must serialise/deserialise from and to an IossEtmpExclusion" in {

      val json = Json.obj(
        "exclusionReason" -> arbEtmpExclusion.exclusionReason,
        "effectiveDate" -> arbEtmpExclusion.effectiveDate,
        "decisionDate" -> arbEtmpExclusion.decisionDate,
        "quarantine" -> arbEtmpExclusion.quarantine
      )

      val expectedResult = IossEtmpExclusion(
        exclusionReason = arbEtmpExclusion.exclusionReason,
        effectiveDate = arbEtmpExclusion.effectiveDate,
        decisionDate = arbEtmpExclusion.decisionDate,
        quarantine = arbEtmpExclusion.quarantine
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[IossEtmpExclusion] mustBe JsSuccess(expectedResult)
    }

    "must handle missing fields" in {

      val json = Json.obj()

      json.validate[IossEtmpExclusion] mustBe a[JsError]
    }

    "must handle invalid fields" in {

      val json = Json.obj(
        "exclusionReason" -> 12345,
        "effectiveDate" -> arbEtmpExclusion.effectiveDate,
        "decisionDate" -> arbEtmpExclusion.decisionDate,
        "quarantine" -> arbEtmpExclusion.quarantine
      )

      json.validate[IossEtmpExclusion] mustBe a[JsError]
    }

    "must handle null fields" in {

      val json = Json.obj(
        "exclusionReason" -> arbEtmpExclusion.exclusionReason,
        "effectiveDate" -> JsNull,
        "decisionDate" -> arbEtmpExclusion.decisionDate,
        "quarantine" -> arbEtmpExclusion.quarantine
      )

      json.validate[IossEtmpExclusion] mustBe a[JsError]
    }
  }

  "IossEtmpExclusionReason" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(IossEtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>
          JsString(etmpExclusionReason.toString).validate[IossEtmpExclusionReason].asOpt.value mustBe etmpExclusionReason
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String].suchThat(!IossEtmpExclusionReason.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValues =>

          JsString(invalidValues).validate[IossEtmpExclusionReason] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(IossEtmpExclusionReason.values)

      forAll(gen) {
        etmpExclusionReason =>

          Json.toJson(etmpExclusionReason) mustBe JsString(etmpExclusionReason.toString)
      }
    }
  }
}
