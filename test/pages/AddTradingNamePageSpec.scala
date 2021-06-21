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
import controllers.euDetails.{routes => euRoutes}
import controllers.routes
import models.{CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class AddTradingNamePageSpec extends SpecBase with PageBehaviours {

  "AddTradingNamePage" - {

    beRetrievable[Boolean](AddTradingNamePage)

    beSettable[Boolean](AddTradingNamePage)

    beRemovable[Boolean](AddTradingNamePage)

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to Trading Name with index equal to the number of names already answered" in {

          val answers =
            emptyUserAnswers
              .set(TradingNamePage(Index(0)), "foo").success.value
              .set(TradingNamePage(Index(1)), "bar").success.value
              .set(AddTradingNamePage, true).success.value

          AddTradingNamePage.navigate(NormalMode, answers)
            .mustEqual(routes.TradingNameController.onPageLoad(NormalMode, Index(2)))
        }
      }

      "when the answer is no" - {

        "to Date of First Sale" in {

          val answers = emptyUserAnswers.set(AddTradingNamePage, false).success.value

          AddTradingNamePage.navigate(NormalMode, answers)
            .mustEqual(routes.DateOfFirstSaleController.onPageLoad(NormalMode))
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Trading Name with index equal to the number of names already answered" in {

          val answers =
            emptyUserAnswers
              .set(TradingNamePage(Index(0)), "foo").success.value
              .set(TradingNamePage(Index(1)), "bar").success.value
              .set(AddTradingNamePage, true).success.value

          AddTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.TradingNameController.onPageLoad(CheckMode, Index(2)))
        }
      }

      "when the answer is no" - {

        "to Check Your Answers" in {

          val answers = emptyUserAnswers.set(AddTradingNamePage, false).success.value

          AddTradingNamePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }
    }
  }
}
