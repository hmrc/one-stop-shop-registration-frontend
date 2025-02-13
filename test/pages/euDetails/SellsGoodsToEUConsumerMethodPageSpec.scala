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
import controllers.euDetails.routes
import controllers.routes as recoveryRoute
import controllers.amend.routes as amendRoute
import controllers.rejoin.routes as rejoinRoute
import models.euDetails.EuConsumerSalesMethod
import models.{AmendLoopMode, AmendMode, Index, NormalMode, RejoinLoopMode, RejoinMode}
import pages.behaviours.PageBehaviours

class SellsGoodsToEUConsumerMethodPageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "SellsGoodsToEUConsumerMethodPage" - {

    beRetrievable[EuConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beSettable[EuConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beRemovable[EuConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    "must navigate" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(NormalMode, countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
      }

      "to Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(recoveryRoute.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend Mode" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(AmendMode, countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendMode, countryIndex))
      }

      "to Amend Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendMode, answers)
          .mustEqual(amendRoute.AmendJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Amend Loop Mode" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(AmendLoopMode, countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendLoopMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendLoopMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendLoopMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(AmendLoopMode, countryIndex))
      }

      "to Amend Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(AmendLoopMode, answers)
          .mustEqual(amendRoute.AmendJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Rejoin Mode" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(RejoinMode, countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinMode, countryIndex))
      }

      "to Rejoin Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinMode, answers)
          .mustEqual(rejoinRoute.RejoinJourneyRecoveryController.onPageLoad())
      }

    }

    "must navigate in Rejoin Loop Mode" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(RejoinLoopMode, countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinLoopMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinLoopMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinLoopMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(RejoinLoopMode, countryIndex))
      }

      "to Rejoin Journey Recovery when there are no answers" in {

        val answers = emptyUserAnswers

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(RejoinLoopMode, answers)
          .mustEqual(rejoinRoute.RejoinJourneyRecoveryController.onPageLoad())
      }

    }

  }
}
