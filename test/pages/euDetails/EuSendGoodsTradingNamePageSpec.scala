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

package pages.euDetails

import base.SpecBase
import models.{AmendMode, CheckLoopMode, CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class EuSendGoodsTradingNamePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "EuSendGoodsTradingNamePage" - {

    beRetrievable[String](EuSendGoodsTradingNamePage(index))

    beSettable[String](EuSendGoodsTradingNamePage(index))

    beRemovable[String](EuSendGoodsTradingNamePage(index))

    "must navigate in Normal Mode" - {
      "to Eu Send Goods Address" in {
        EuSendGoodsTradingNamePage(index).navigate(NormalMode, emptyUserAnswers)mustBe
          controllers.euDetails.routes.EuSendGoodsAddressController.onPageLoad(NormalMode, index)
      }
    }

    "must navigate in Check Mode" - {
      "to Eu Send Goods Address if it hasn't been answered" in {
        EuSendGoodsTradingNamePage(index).navigate(CheckMode, emptyUserAnswers)mustBe
          controllers.euDetails.routes.EuSendGoodsAddressController.onPageLoad(CheckMode, index)
      }

      "to wherever Eu Send Goods Address navigates if it has been answered" in {
        val address = arbitraryInternationalAddress.arbitrary.sample.value
        EuSendGoodsTradingNamePage(index).navigate(CheckMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value) mustBe
          EuSendGoodsAddressPage(index).navigate(CheckMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value)
      }
    }

    "must navigate in Check Loop Mode" - {
      "to Eu Send Goods Address if it hasn't been answered" in {
        EuSendGoodsTradingNamePage(index).navigate(CheckLoopMode, emptyUserAnswers)mustBe
          controllers.euDetails.routes.EuSendGoodsAddressController.onPageLoad(CheckLoopMode, index)
      }

      "to wherever Eu Send Goods Address navigates if it has been answered" in {
        val address = arbitraryInternationalAddress.arbitrary.sample.value
        EuSendGoodsTradingNamePage(index).navigate(CheckLoopMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value) mustBe
          EuSendGoodsAddressPage(index).navigate(CheckLoopMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value)
      }
    }

    "must navigate in Amend Mode" - {
      "to Eu Send Goods Address if it hasn't been answered" in {
        EuSendGoodsTradingNamePage(index).navigate(AmendMode, emptyUserAnswers) mustBe
          controllers.euDetails.routes.EuSendGoodsAddressController.onPageLoad(AmendMode, index)
      }

      "to wherever Eu Send Goods Address navigates if it has been answered" in {
        val address = arbitraryInternationalAddress.arbitrary.sample.value
        EuSendGoodsTradingNamePage(index).navigate(AmendMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value) mustBe
          EuSendGoodsAddressPage(index).navigate(AmendMode, emptyUserAnswers.set(EuSendGoodsAddressPage(index), address).success.value)
      }
    }
  }
}
