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

package services

import base.SpecBase
import models.exclusions.{ExcludedTrader, ExclusionReason}
import org.scalatestplus.mockito.MockitoSugar

import java.time.LocalDate

class RejoinRegistrationServiceSpec extends SpecBase
  with MockitoSugar {

  private val service = new RejoinRegistrationService()
  private val currentDate = LocalDate.now()

  "RejoinRegistrationService" - {

    "return false for canRejoinRegistration when exclusion reason is Reversal" in {
      val exclusion = ExcludedTrader(vrn, ExclusionReason.Reversal, effectiveDate = currentDate.minusYears(3), quarantined = false)
      service.canRejoinRegistration(currentDate, Some(exclusion)) mustBe false
    }

    "return true for canRejoinRegistration when quarantined and after two years" in {
      val exclusion = ExcludedTrader(vrn, ExclusionReason.FailsToComply, effectiveDate = currentDate.minusYears(3), quarantined = true)
      service.canRejoinRegistration(currentDate, Some(exclusion)) mustBe true
    }

    "return false for canRejoinRegistration when quarantined but less than two years have passed" in {
      val exclusion = ExcludedTrader(vrn, ExclusionReason.FailsToComply, effectiveDate = currentDate.minusYears(1), quarantined = true)
      service.canRejoinRegistration(currentDate, Some(exclusion)) mustBe false
    }

    "return true for canRejoinRegistration when not quarantined and effective date is in the past" in {
      val exclusion = ExcludedTrader(vrn, ExclusionReason.CeasedTrade, effectiveDate = currentDate.minusDays(1), quarantined = false)
      service.canRejoinRegistration(currentDate, Some(exclusion)) mustBe true
    }

    "return false for canRejoinRegistration when not quarantined and effective date is in the future" in {
      val exclusion = ExcludedTrader(vrn, ExclusionReason.CeasedTrade, effectiveDate = currentDate.plusDays(1), quarantined = false)
      service.canRejoinRegistration(currentDate, Some(exclusion)) mustBe false
    }

    "return false for canRejoinRegistration when exclusion is None" in {
      service.canRejoinRegistration(currentDate, None) mustBe false
    }

    "return true for canReverse when date of first sale is before the effective date" in {
      val exclusion = Some(ExcludedTrader(vrn, ExclusionReason.NoLongerSupplies, effectiveDate = currentDate, quarantined = false))
      service.canReverse(currentDate.minusDays(1), exclusion) mustBe true
    }

    "return false for canReverse when date of first sale is after or equal to the effective date" in {
      val exclusion = Some(ExcludedTrader(vrn, ExclusionReason.VoluntarilyLeaves, effectiveDate = currentDate, quarantined = false))
      service.canReverse(currentDate, exclusion) mustBe false
      service.canReverse(currentDate.plusDays(1), exclusion) mustBe false
    }

    "return false for canReverse when exclusion is None" in {
      service.canReverse(currentDate, None) mustBe false
    }
  }
}
