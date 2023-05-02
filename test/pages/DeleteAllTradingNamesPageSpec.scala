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
import controllers.amend.{routes => amendRoutes}
import models.{AmendMode, CheckMode, Index}
import pages.behaviours.PageBehaviours

class DeleteAllTradingNamesPageSpec extends SpecBase with PageBehaviours {

  "DeleteAllTradingNamesPage" - {

    beRetrievable[Boolean](DeleteAllTradingNamesPage)

    beSettable[Boolean](DeleteAllTradingNamesPage)

    beRemovable[Boolean](DeleteAllTradingNamesPage)

    "must navigate in CheckMode" - {

      "to Check Your Answers Page when the user answers Yes" in {

        val answers = emptyUserAnswers
          .set(TradingNamePage(Index(0)), "foo trading name").success.value
          .set(TradingNamePage(Index(1)), "bar trading name").success.value
          .set(DeleteAllTradingNamesPage, true).success.value

        DeleteAllTradingNamesPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Check Your Answers Page when the user answers No" in {

        val answers = emptyUserAnswers
          .set(TradingNamePage(Index(0)), "foo trading Name").success.value
          .set(TradingNamePage(Index(1)), "bar trading Name").success.value
          .set(DeleteAllTradingNamesPage, false).success.value

        DeleteAllTradingNamesPage.navigate(CheckMode, answers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllTradingNamesPage.navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend" - {

      "to Change Your Registration Page when the user answers Yes" in {

        val answers = emptyUserAnswers
          .set(TradingNamePage(Index(0)), "foo trading name").success.value
          .set(TradingNamePage(Index(1)), "bar trading name").success.value
          .set(DeleteAllTradingNamesPage, true).success.value

        DeleteAllTradingNamesPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Change Your Registration Page when the user answers No" in {

        val answers = emptyUserAnswers
          .set(TradingNamePage(Index(0)), "foo trading Name").success.value
          .set(TradingNamePage(Index(1)), "bar trading Name").success.value
          .set(DeleteAllTradingNamesPage, false).success.value

        DeleteAllTradingNamesPage.navigate(AmendMode, answers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }

      "to Journey Recovery Page when the user submits no answer" in {

        val answers = emptyUserAnswers

        DeleteAllTradingNamesPage.navigate(AmendMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }
  }
}
