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
import models.euDetails.EuConsumerSalesMethod
import models.{AmendLoopMode, AmendMode, CheckLoopMode, CheckMode, Index, NormalMode, RejoinLoopMode, RejoinMode}
import pages.behaviours.PageBehaviours

class EuTaxReferencePageSpec extends SpecBase with PageBehaviours {

  private val countryIndex = Index(0)

  "EuTaxReferencePage" - {

    beRetrievable[String](EuTaxReferencePage(countryIndex))

    beSettable[String](EuTaxReferencePage(countryIndex))

    beRemovable[String](EuTaxReferencePage(countryIndex))

    "must navigate in Normal mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, countryIndex))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex))
        }

      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex))
        }
      }

      "to Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, countryIndex))
        }

        "to wherever Fixed Establishment Trading Name navigates when Sells Goods To EU Consumer Method is Fixed Establishment and it has been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
            .set(FixedEstablishmentTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckMode, answers))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {
          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers))
        }
      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers))
        }

      }

      "to Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(CheckMode, answers)
          .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Check Loop mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
        }

        "to wherever Fixed Establishment Trading Name navigates when Sells Goods To EU Consumer Method is Fixed Establishment and it has been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
            .set(FixedEstablishmentTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {
          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
        }
      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
        }

      }

      "to Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(CheckLoopMode, answers)
          .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendMode, countryIndex))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
        }

      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
        }
      }

      "to Amend Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend Loop mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendLoopMode, countryIndex))
        }

        "to wherever Fixed Establishment Trading Name navigates when Sells Goods To EU Consumer Method is Fixed Establishment and it has been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
            .set(FixedEstablishmentTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(AmendLoopMode, answers))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {
          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendLoopMode, answers))
        }
      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendLoopMode, answers))
        }

      }

      "to Amend Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Rejoin mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(RejoinMode, countryIndex))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(RejoinMode, countryIndex))
        }

      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(RejoinMode, countryIndex))
        }
      }

      "to Rejoin Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Rejoin Loop mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(RejoinLoopMode, countryIndex))
        }

        "to wherever Fixed Establishment Trading Name navigates when Sells Goods To EU Consumer Method is Fixed Establishment and it has been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
            .set(FixedEstablishmentTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(RejoinLoopMode, answers))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(RejoinLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {
          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(RejoinLoopMode, answers))
        }
      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(RejoinLoopMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(RejoinLoopMode, answers))
        }

      }

      "to Rejoin Journey Recovery when there are no answers" in {
        val answers = emptyUserAnswers

        EuTaxReferencePage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
      }
    }
  }
}
