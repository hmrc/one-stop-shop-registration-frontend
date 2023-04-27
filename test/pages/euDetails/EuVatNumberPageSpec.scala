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
import pages.euDetails

class EuVatNumberPageSpec extends SpecBase with PageBehaviours {

  val countryIndex: Index = Index(0)

  "EuVatNumberPage" - {

    beRetrievable[String](EuVatNumberPage(countryIndex))

    beSettable[String](euDetails.EuVatNumberPage(countryIndex))

    beRemovable[String](euDetails.EuVatNumberPage(countryIndex))

    "must navigate in Normal mode" - {

      "when user is not part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check EU Details for the same index" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(NormalMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to Fixed Establishment Trading Name for the same index when Sells Goods To EU Consumer Method is Fixed Establishment" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

            EuVatNumberPage(countryIndex).navigate(NormalMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, countryIndex))
          }

          "to EU Send Goods Trading Name for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(NormalMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex))
          }

        }

      }

      "when user is part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check Eu Details Answers for the same index" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(NormalMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to EU Send Goods Trading Name for the same index" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(NormalMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, countryIndex))
          }

        }

      }
    }

    "must navigate in Check mode" - {

      "when user is not part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check EU Details for the same index" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to Fixed Establishment Trading Name for the same index when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, countryIndex))
          }

          "to wherever Fixed Establishment Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Fixed Establishment when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
              .set(FixedEstablishmentTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckMode, answers))
          }

          "to EU Send Goods Trading Name for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers))
          }

        }

      }

      "when user is part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check Eu Details Answers for the same index" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to EU Send Goods Trading Name for the same index and it has not been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate to for the same index when it has been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckMode, answers))
          }

        }

      }

    }

    "must navigate in Check Loop mode" - {

      "when user is not part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check EU Details for the same index" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckLoopMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to Fixed Establishment Trading Name for the same index when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
          }

          "to wherever Fixed Establishment Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Fixed Establishment when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
              .set(FixedEstablishmentTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
          }

          "to EU Send Goods Trading Name for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
          }

        }

      }

      "when user is part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check Eu Details Answers for the same index" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckLoopMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to EU Send Goods Trading Name for the same index and it has not been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate to for the same index when it has been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(CheckLoopMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(CheckLoopMode, answers))
          }

        }

      }

    }

    "must navigate in Amend mode" - {

      "when user is not part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check EU Details for the same index" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to Fixed Establishment Trading Name for the same index when Sells Goods To EU Consumer Method is Fixed Establishment and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(AmendMode, countryIndex))
          }

          "to wherever Fixed Establishment Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Fixed Establishment when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value
              .set(FixedEstablishmentTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(FixedEstablishmentTradingNamePage(countryIndex).navigate(AmendMode, answers))
          }

          "to EU Send Goods Trading Name for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch and it has not been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate for the same index when Sells Goods To EU Consumer Method is Warehouse Dispatch when it has been answered" in {

            val answers = emptyUserAnswers
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendMode, answers))
          }

        }

      }

      "when user is part of VAT group" - {

        "when user does not sell goods to consumers in the EU" - {

          "to Check Eu Details Answers for the same index" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, countryIndex))
          }

        }

        "when user sells goods to consumers in the EU" - {

          "to EU Send Goods Trading Name for the same index and it has not been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(AmendMode, countryIndex))
          }

          "to wherever EU Send Goods Trading Name would navigate to for the same index when it has been answered" in {

            val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
              .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
              .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
              .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value

            EuVatNumberPage(countryIndex).navigate(AmendMode, answers)
              .mustEqual(EuSendGoodsTradingNamePage(countryIndex).navigate(AmendMode, answers))
          }

        }

      }

    }

  }

}
