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
import models.core.{Match, TraderId}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect

import java.time.LocalDate

class RejoinRedirectServiceSpec extends SpecBase
  with MockitoSugar {

  private val genericMatch = Match(
    TraderId("33333333"),
    None,
    "DE",
    None,
    None,
    exclusionEffectiveDate = Some(LocalDate.now),
    None,
    None
  )

  ".redirectOnMatch" - {

    "redirects to RejoinAlreadyRegisteredOtherCountryController when the match is an active trader" in {
      RejoinRedirectService.redirectOnMatch(
        maybeMatch = Some(genericMatch),
        clock = stubClockAtArbitraryDate
      ).value mustBe
        Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
    }

    "redirects to CannotRejoinQuarantinedCountryController when the match is a quarantined trader" in {
      RejoinRedirectService.redirectOnMatch(
        maybeMatch = Some(genericMatch.copy(exclusionStatusCode = Some(4))),
        clock = stubClockAtArbitraryDate
      ).value mustBe
        Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
          genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
    }

    "throw an IllegalStateException when the matchType is quarantined but exclusionEffectiveDate is missing" in {
      val exclusionStatusCode = Some(4)
      val exception = intercept[IllegalStateException] {
        RejoinRedirectService.redirectOnMatch(
          maybeMatch = Some(genericMatch.copy(exclusionStatusCode = exclusionStatusCode, exclusionEffectiveDate = None)),
          clock = stubClockAtArbitraryDate
        )
      }
      exception.getMessage must include(s"Exclusion status code $exclusionStatusCode didn't include an expected exclusion effective date")
    }
  }
}
