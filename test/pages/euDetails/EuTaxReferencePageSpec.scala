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
import controllers.euDetails.{routes => euRoutes}
import models.euDetails.EuConsumerSalesMethod
import models.{AmendMode, CheckLoopMode, CheckMode, Index, NormalMode}
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
    }

    "must navigate in Amend mode" - {

      "when user is not part of VAT group" - {

        "to Fixed Establishment Trading Name when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendMode, countryIndex))
        }

        "to wherever Fixed Establishment Trading Name navigates when Sells Goods To EU Consumer Method is Fixed Establishment and it has been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
            .set(FixedEstablishmentTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(AmendMode, answers))
        }

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {
          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendMode, answers))
        }
      }

      "when user is part of VAT group" - {

        "to Eu Send Goods Trading Name when Sells Goods To EU Consumer Method is DispatchWarehouse and it has not been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
        }

        "to wherever Eu Send Goods Trading Name navigates when Sells Goods To EU Consumer Method is DispatchWarehouse and it has been answered" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
            .set(EuSendGoodsTradingNamePage(countryIndex), "foo").success.value

          EuTaxReferencePage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendMode, answers))
        }

      }

    }
  }
}
