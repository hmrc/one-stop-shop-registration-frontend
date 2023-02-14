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
import controllers.euDetails.routes
import models.euDetails.EUConsumerSalesMethod
import models.{Index, NormalMode}
import pages.behaviours.PageBehaviours

class SellsGoodsToEUConsumerMethodPageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "SellsGoodsToEUConsumerMethodPage" - {

    beRetrievable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beSettable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beRemovable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    "must navigate" - {

      "when user is part of VAT group" - {

        "to Cannot Add Country when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EUConsumerSalesMethod.FixedEstablishment).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.CannotAddCountryController.onPageLoad(countryIndex))
        }

        "to Registration Type when user answers Dispatch Warehouse" in {

          val answers = emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EUConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
        }
      }

      "when user is not part of VAT group" - {

        "to Registration Type when user answers Fixed Establishment" in {

          val answers = emptyUserAnswers
            .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EUConsumerSalesMethod.DispatchWarehouse).success.value

          SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
            .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
        }
      }

      "to Registration Type when user answers Dispatch Warehouse" in {

        val answers = emptyUserAnswers
          .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EUConsumerSalesMethod.DispatchWarehouse).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex))
      }

    }
  }
}
