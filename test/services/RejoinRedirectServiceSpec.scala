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
import models.core.{Match, MatchType}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect

import java.time.LocalDate

class RejoinRedirectServiceSpec extends SpecBase
  with MockitoSugar {

  private val genericMatch = Match(
    MatchType.TransferringMSID,
    "33333333",
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
      Seq(MatchType.FixedEstablishmentActiveNETP, MatchType.TraderIdActiveNETP, MatchType.OtherMSNETPActiveNETP).map { matchType =>
        RejoinRedirectService.redirectOnMatch(Some(genericMatch.copy(matchType = matchType))).value mustBe
          Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
      }
    }

    "redirects to CannotRejoinQuarantinedCountryController when the match is a quarantined trader" in {
      Seq(MatchType.FixedEstablishmentQuarantinedNETP, MatchType.TraderIdQuarantinedNETP, MatchType.OtherMSNETPQuarantinedNETP).map { matchType =>
        RejoinRedirectService.redirectOnMatch(Some(genericMatch.copy(matchType = matchType))).value mustBe
          Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
      }
   }
  }
}
