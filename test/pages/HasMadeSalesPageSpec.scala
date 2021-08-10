/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

class HasMadeSalesPageSpec extends SpecBase with PageBehaviours {

  "HasMadeSalesPage" - {

    beRetrievable[Boolean](HasMadeSalesPage)

    beSettable[Boolean](HasMadeSalesPage)

    beRemovable[Boolean](HasMadeSalesPage)

    "must navigate to Date Of First Sale page when the answer is yes" in {

      HasMadeSalesPage.navigate(
        NormalMode,
        emptyUserAnswers.set(
          HasMadeSalesPage,
          true
        ).success.value) mustEqual controllers.routes.DateOfFirstSaleController.onPageLoad(NormalMode)
    }

    "must navigate to Is Planning First Eligible Sale page when the answer is no" in {

      HasMadeSalesPage.navigate(NormalMode,
        emptyUserAnswers.set(
          HasMadeSalesPage,
          false
        ).success.value) mustEqual controllers.routes.IsPlanningFirstEligibleSaleController.onPageLoad(NormalMode)
    }

    "must navigate in Check mode" - {

      "to Check Your Answers" in {

        IsOnlineMarketplacePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}
