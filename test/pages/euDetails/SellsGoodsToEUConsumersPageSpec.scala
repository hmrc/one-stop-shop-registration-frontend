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
import controllers.euDetails.routes as euRoutes
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import controllers.routes
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, RejoinLoopMode, RejoinMode}
import pages.behaviours.PageBehaviours

class SellsGoodsToEUConsumersPageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "SellsGoodsToEUConsumersPage" - {

    beRetrievable[Boolean](SellsGoodsToEUConsumersPage(countryIndex))

    beSettable[Boolean](SellsGoodsToEUConsumersPage(countryIndex))

    beRemovable[Boolean](SellsGoodsToEUConsumersPage(countryIndex))

    "must navigate in Normal Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(NormalMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(NormalMode, countryIndex))
      }

      "to Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Check Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(CheckMode, countryIndex))
      }

      "to Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Check Loop Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckLoopMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(CheckLoopMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckLoopMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(NormalMode, countryIndex))
      }

      "to Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(CheckLoopMode, answers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Amend Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(AmendMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(AmendMode, countryIndex))
      }

      "to Amend Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Amend Loop Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(AmendLoopMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(AmendMode, countryIndex))
      }

      "to Amend Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Rejoin Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(RejoinMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(RejoinMode, countryIndex))
      }

      "to Rejoin Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Rejoin Loop Mode" - {

      "to Sells Goods To EU Consumer Method when answer is Yes" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(euRoutes.SellsGoodsToEUConsumerMethodController.onPageLoad(RejoinLoopMode, countryIndex))
      }

      "to Sales Declaration Not Required when answer is No" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(euRoutes.SalesDeclarationNotRequiredController.onPageLoad(RejoinMode, countryIndex))
      }

      "to Rejoin Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumersPage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
      }

    }

  }

}
