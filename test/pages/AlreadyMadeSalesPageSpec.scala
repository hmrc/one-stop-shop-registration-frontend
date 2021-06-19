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
import models.AlreadyMadeSales.{No, Yes}
import models.{AlreadyMadeSales, CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class AlreadyMadeSalesPageSpec extends SpecBase with PageBehaviours {

  "AlreadyMadeSalesPage" - {

    beRetrievable[AlreadyMadeSales](AlreadyMadeSalesPage)

    beSettable[AlreadyMadeSales](AlreadyMadeSalesPage)

    beRemovable[AlreadyMadeSales](AlreadyMadeSalesPage)

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to Commencement Date" in {

          val answers = emptyUserAnswers.set(AlreadyMadeSalesPage, Yes(LocalDate.now)).success.value
          AlreadyMadeSalesPage.navigate(NormalMode, answers)
            .mustEqual(routes.CommencementDateController.onPageLoad(NormalMode))
        }
      }

      "when the answer is no" - {

        "to Intend to Sell Goods This Quarter" in {

          val answers = emptyUserAnswers.set(AlreadyMadeSalesPage, No).success.value
          AlreadyMadeSalesPage.navigate(NormalMode, answers)
            .mustEqual(routes.IntendToSellGoodsThisQuarterController.onPageLoad(NormalMode))
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Commencement Date" in {

          val answers = emptyUserAnswers.set(AlreadyMadeSalesPage, Yes(LocalDate.now)).success.value
          AlreadyMadeSalesPage.navigate(CheckMode, answers)
            .mustEqual(routes.CommencementDateController.onPageLoad(CheckMode))
        }
      }

      "when the answer is no" - {

        "and Intend to Sell Goods has been answered" - {

          "to Check Your Answers" in {

            val answers =
              emptyUserAnswers
                .set(IntendToSellGoodsThisQuarterPage, true).success.value
                .set(AlreadyMadeSalesPage, No).success.value

            AlreadyMadeSalesPage.navigate(CheckMode, answers)
              .mustEqual(routes.CheckYourAnswersController.onPageLoad())
          }
        }

        "and Intend to Sell Goods has not been answered" - {

          "to Intend to Sell Goods This Quarter" in {

            val answers = emptyUserAnswers.set(AlreadyMadeSalesPage, No).success.value
            AlreadyMadeSalesPage.navigate(CheckMode, answers)
              .mustEqual(routes.IntendToSellGoodsThisQuarterController.onPageLoad(CheckMode))
          }
        }
      }
    }

    "must remove Intend to Sell Goods when the answer is yes" in {

      val answers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, true).success.value

      val result = answers.set(AlreadyMadeSalesPage, Yes(LocalDate.now)).success.value

      result.get(IntendToSellGoodsThisQuarterPage) must not be defined
    }
  }
}
