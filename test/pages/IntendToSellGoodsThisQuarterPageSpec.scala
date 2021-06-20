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

class IntendToSellGoodsThisQuarterPageSpec extends SpecBase with PageBehaviours {

  "IntendToSellGoodsThisQuarterPage" - {

    beRetrievable[Boolean](IntendToSellGoodsThisQuarterPage)

    beSettable[Boolean](IntendToSellGoodsThisQuarterPage)

    beRemovable[Boolean](IntendToSellGoodsThisQuarterPage)

    "must navigate in Normal mode" - {

      "to Commencement Date when the answer is yes" in {

        val answers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, true).success.value

        IntendToSellGoodsThisQuarterPage.navigate(NormalMode, answers)
          .mustEqual(routes.CommencementDateController.onPageLoad(NormalMode))
      }

      "to Register Later when the answer is no" in {

        val answers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, false).success.value

        IntendToSellGoodsThisQuarterPage.navigate(NormalMode, answers)
          .mustEqual(routes.RegisterLaterController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "to Commencement Date when the answer is yes" in {

        val answers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, true).success.value

        IntendToSellGoodsThisQuarterPage.navigate(CheckMode, answers)
          .mustEqual(routes.CommencementDateController.onPageLoad(CheckMode))
      }
    }

    "to Register Later when the answer is no" in {

      val answers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, false).success.value

      IntendToSellGoodsThisQuarterPage.navigate(CheckMode, answers)
        .mustEqual(routes.RegisterLaterController.onPageLoad())
    }
  }
}
