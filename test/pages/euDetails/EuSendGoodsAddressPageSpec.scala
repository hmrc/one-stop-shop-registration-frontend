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
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, InternationalAddress, NormalMode, RejoinLoopMode, RejoinMode}
import pages.behaviours.PageBehaviours

class EuSendGoodsAddressPageSpec extends SpecBase with PageBehaviours {

  private val index: Index = Index(0)

  "EuSendGoodsAddressPage" - {

    beRetrievable[InternationalAddress](EuSendGoodsAddressPage(index))

    beSettable[InternationalAddress](EuSendGoodsAddressPage(index))

    beRemovable[InternationalAddress](EuSendGoodsAddressPage(index))

    "must navigate in Normal Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(NormalMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
      }
    }

    "must navigate in Check Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(CheckMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index)
      }
    }

    "must navigate in Check Loop Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(CheckLoopMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
      }
    }

    "must navigate in Amend Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(AmendMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, index)
      }
    }

    "must navigate in Amend Loop Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(AmendLoopMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, index)
      }
    }

    "must navigate in Rejoin Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(RejoinMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(RejoinMode, index)
      }
    }

    "must navigate in Rejoin Loop Mode" - {
      "to Check Eu Details Answers" in {
        EuSendGoodsAddressPage(index).navigate(RejoinLoopMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(RejoinMode, index)
      }
    }
  }
}
