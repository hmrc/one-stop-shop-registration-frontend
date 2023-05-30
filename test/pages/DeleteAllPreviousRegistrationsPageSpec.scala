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

package pages

import base.SpecBase
import controllers.routes
import models.previousRegistrations.PreviousSchemeNumbers
import models.{CheckMode, Country, Index}
import pages.behaviours.PageBehaviours
import pages.previousRegistrations._

class DeleteAllPreviousRegistrationsPageSpec extends SpecBase with PageBehaviours {

  "DeleteAllPreviousRegistrationsPage" - {

    beRetrievable[Boolean](DeleteAllPreviousRegistrationsPage)

    beSettable[Boolean](DeleteAllPreviousRegistrationsPage)

    beRemovable[Boolean](DeleteAllPreviousRegistrationsPage)

    "must navigate in CheckMode" - {

      "to Check Your Answers Page when the user answers Yes" in {

        val answers = emptyUserAnswers
          .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("DE123", None)).success.value
          .set(DeleteAllPreviousRegistrationsPage, true).success.value

        DeleteAllPreviousRegistrationsPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Check Your Answers Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(PreviousEuCountryPage(Index(0)), Country("ES", "Spain")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("ES123", None)).success.value
          .set(DeleteAllPreviousRegistrationsPage, false).success.value

        DeleteAllPreviousRegistrationsPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers
          .set(PreviousEuCountryPage(Index(0)), Country("EE", "Estonia")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("EE123", None)).success.value

        DeleteAllPreviousRegistrationsPage.navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
