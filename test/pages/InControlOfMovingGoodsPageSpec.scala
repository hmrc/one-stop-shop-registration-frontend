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
import models.NormalMode
import pages.behaviours.PageBehaviours

class InControlOfMovingGoodsPageSpec extends SpecBase with PageBehaviours {

  "InControlOfMovingGoodsPage" - {

    beRetrievable[Boolean](InControlOfMovingGoodsPage)

    beSettable[Boolean](InControlOfMovingGoodsPage)

    beRemovable[Boolean](InControlOfMovingGoodsPage)

    "must navigate in Normal Mode" - {

      "to Check Vat Details when the answer is yes and we have the user's VAT info" in {

        val answers = emptyUserAnswersWithVatInfo.set(InControlOfMovingGoodsPage, true).success.value
        InControlOfMovingGoodsPage.navigate(NormalMode, answers)
          .mustEqual(routes.CheckVatDetailsController.onPageLoad(NormalMode))
      }

      "to Check Vat Number when the answer is yes and we do not have the user's VAT info" in {

        val answers = emptyUserAnswers.set(InControlOfMovingGoodsPage, true).success.value
        InControlOfMovingGoodsPage.navigate(NormalMode, answers)
          .mustEqual(routes.CheckVatNumberController.onPageLoad(NormalMode))
      }

      "to Cannot Register when the answer is no" in {

        val answers = emptyUserAnswers.set(InControlOfMovingGoodsPage, false).success.value
        InControlOfMovingGoodsPage.navigate(NormalMode, answers)
          .mustEqual(routes.CannotRegisterForServiceController.onPageLoad())
      }
    }
  }
}
