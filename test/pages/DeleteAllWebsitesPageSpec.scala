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

package pages

import base.SpecBase
import controllers.routes
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import models.{AmendMode, CheckMode, Index, RejoinMode}
import pages.behaviours.PageBehaviours

class DeleteAllWebsitesPageSpec extends SpecBase with PageBehaviours {

  "DeleteAllWebsitesPage" - {

    beRetrievable[Boolean](DeleteAllWebsitesPage)

    beSettable[Boolean](DeleteAllWebsitesPage)

    beRemovable[Boolean](DeleteAllWebsitesPage)

    "must navigate in CheckMode" - {

      "to Check Your Answers Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, true).success.value

        DeleteAllWebsitesPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Check Your Answers Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, false).success.value

        DeleteAllWebsitesPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllWebsitesPage.navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in AmendMode" - {

      "to Change Your Registration Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, true).success.value

        DeleteAllWebsitesPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Change Your Registration Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, false).success.value

        DeleteAllWebsitesPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Amend Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllWebsitesPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in RejoinMode" - {

      "to Change Your Registration Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, true).success.value

        DeleteAllWebsitesPage.navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
      }

      "to Change Your Registration Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(WebsitePage(Index(0)), "website1").success.value
          .set(WebsitePage(Index(1)), "website2").success.value
          .set(DeleteAllWebsitesPage, false).success.value

        DeleteAllWebsitesPage.navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
      }

      "to Rejoin Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllWebsitesPage.navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
      }
    }

  }
}
