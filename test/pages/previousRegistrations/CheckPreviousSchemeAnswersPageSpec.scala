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

package pages.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.amend.{routes => amendRoutes}
import controllers.routes
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, CheckMode, Country, Index, NormalMode}
import pages.behaviours.PageBehaviours

class CheckPreviousSchemeAnswersPageSpec extends SpecBase with PageBehaviours {

  "CheckPreviousSchemeAnswersPage" - {

    beRetrievable[Boolean](PreviouslyRegisteredPage)

    beSettable[Boolean](PreviouslyRegisteredPage)

    beRemovable[Boolean](PreviouslyRegisteredPage)

    "must navigate in Normal mode" - {

      "to Previous Scheme Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), true).success.value
          .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(NormalMode, Index(0), Index(1)))
      }

      "to Add Previous Registration Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), false).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
      }

      "to Journey Recovery Page when user does not answer" in {

        val answers = emptyUserAnswers

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Check mode" - {

      "to Previous Scheme Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), true).success.value
          .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(CheckMode, Index(0), Index(1)))
      }

      "to Add Previous Registration Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), false).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(CheckMode))
      }

      "to Journey Recovery Page when user does not answer" in {

        val answers = emptyUserAnswers

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Amend mode" - {

      "to Previous Scheme Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), true).success.value
          .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(AmendMode, answers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(AmendMode, Index(0), Index(1)))
      }

      "to Add Previous Registration Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(CheckPreviousSchemeAnswersPage(Index(0)), false).success.value

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(AmendMode, answers)
          .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(AmendMode))
      }

      "to Amend Journey Recovery Page when user does not answer" in {

        val answers = emptyUserAnswers

        CheckPreviousSchemeAnswersPage(Index(0)).navigate(AmendMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }

    }
  }
}
