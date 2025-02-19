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

package pages.euDetails

import base.SpecBase
import controllers.routes
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import models.{AmendMode, CheckMode, Country, Index, RejoinMode}
import pages.behaviours.PageBehaviours

class DeleteAllEuDetailsPageSpec extends SpecBase with PageBehaviours {

  "DeleteAllEuDetailsPage" - {

    beRetrievable[Boolean](DeleteAllEuDetailsPage)

    beSettable[Boolean](DeleteAllEuDetailsPage)

    beRemovable[Boolean](DeleteAllEuDetailsPage)

    "must navigate in CheckMode" - {

      "to Check Your Answers Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, true).success.value

        DeleteAllEuDetailsPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Check Your Answers Page when user answers No" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, false).success.value

        DeleteAllEuDetailsPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllEuDetailsPage.navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in AmendMode" - {

      "to Change Your Registration Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, true).success.value

        DeleteAllEuDetailsPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Change Your Registration when user answers No" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, false).success.value

        DeleteAllEuDetailsPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllEuDetailsPage.navigate(AmendMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in RejoinMode" - {

      "to Change Your Registration Page when user answers Yes" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, true).success.value

        DeleteAllEuDetailsPage.navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
      }

      "to Change Your Registration when user answers No" in {

        val answers = emptyUserAnswers
          .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
          .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
          .set(DeleteAllEuDetailsPage, false).success.value

        DeleteAllEuDetailsPage.navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllEuDetailsPage.navigate(RejoinMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
