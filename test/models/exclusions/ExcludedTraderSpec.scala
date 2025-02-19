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
import models.etmp.EtmpExclusion
import models.{Period, Quarter}
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsError, JsNull, JsString, JsSuccess, Json}

import java.time.LocalDate

class ExcludedTraderSpec extends SpecBase with ScalaCheckPropertyChecks {

  private val genExclusionReason: ExclusionReason = Gen.oneOf(ExclusionReason.values).sample.value
  private val genVrn = arbitraryVrn.arbitrary.sample.value
  private val quarantined: Boolean = arbitrary[Boolean].sample.value
  private val effectiveDate = LocalDate.of(2023, 2, 1)

  "ExcludedTrader" - {

    "must deserialise/serialise to and from ExcludedTrader" in {

      val json = Json.obj(
        "exclusionReason" -> genExclusionReason,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "vrn" -> genVrn,
        "quarantined" -> quarantined
      )

      val expectedResult = ExcludedTrader(
        exclusionReason = genExclusionReason,
        effectiveDate = LocalDate.of(2023, 2, 1),
        vrn = genVrn,
        quarantined = quarantined
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[ExcludedTrader] mustBe JsSuccess(expectedResult)
    }

    "must determine the correct finalReturnPeriod based on exclusionReason" in {
      val date = LocalDate.of(2024, 3, 10)
      val periodForDate = Period(2024, Quarter.Q1)

      val traderWithTransferMSID = ExcludedTrader(genVrn, ExclusionReason.TransferringMSID, date, quarantined)
      traderWithTransferMSID.finalReturnPeriod mustBe periodForDate

      val traderWithOtherReason = ExcludedTrader(genVrn, ExclusionReason.CeasedTrade, date, quarantined)
      traderWithOtherReason.finalReturnPeriod mustBe periodForDate.getPreviousPeriod
    }

    "handle missing values" in {

      val invalidJson = Json.obj()

      invalidJson.validate[ExcludedTrader] mustBe a[JsError]
    }

    "handle invalid values" in {

      val invalidJson = Json.obj(
        "exclusionReason" -> 12345,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "vrn" -> genVrn,
        "quarantined" -> quarantined
      )

      invalidJson.validate[ExcludedTrader] mustBe a[JsError]
    }

    "handle null values" in {

      val invalidJson = Json.obj(
        "exclusionReason" -> JsNull,
        "effectiveDate" -> LocalDate.of(2023, 2, 1),
        "vrn" -> genVrn,
        "quarantined" -> quarantined
      )

      invalidJson.validate[ExcludedTrader] mustBe a[JsError]
    }
    
  }

  "fromEtmpExclusion" - {

    "must create an ExcludedTrader from EtmpExclusion" in {

      val decisionDate = LocalDate.of(2023, 1, 15)

      val etmpExclusion = EtmpExclusion(
        exclusionReason = genExclusionReason,
        effectiveDate = effectiveDate,
        decisionDate = decisionDate,
        quarantine = quarantined
      )

      val excludedTrader = ExcludedTrader.fromEtmpExclusion(genVrn, etmpExclusion)

      excludedTrader.vrn mustBe genVrn
      excludedTrader.exclusionReason mustBe etmpExclusion.exclusionReason
      excludedTrader.effectiveDate mustBe etmpExclusion.effectiveDate
      excludedTrader.quarantined mustBe etmpExclusion.quarantine
      assertDoesNotCompile("excludedTrader.decisionDate")
    }
  }

  "ExclusionReason" - {

    "must deserialise valid values" in {

      val gen = Gen.oneOf(ExclusionReason.values)

      forAll(gen) {
        exclusionReason =>

          JsString(exclusionReason.toString).validate[ExclusionReason].asOpt.value mustBe exclusionReason
      }
    }

    "must fail to deserialise invalid values" in {

      val gen = arbitrary[String] suchThat (!ExclusionReason.values.map(_.toString).contains(_))

      forAll(gen) {
        invalidValue =>

          JsString(invalidValue).validate[ExclusionReason] mustBe JsError("error.invalid")
      }
    }

    "must serialise" in {

      val gen = Gen.oneOf(ExclusionReason.values)

      forAll(gen) {
        exclusionReason =>

          Json.toJson(exclusionReason) mustBe JsString(exclusionReason.toString)
      }
    }
  }
}