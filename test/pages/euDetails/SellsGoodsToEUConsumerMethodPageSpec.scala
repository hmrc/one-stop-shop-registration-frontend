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
import models.Index
import models.euDetails.EUConsumerSalesMethod
import models.euDetails.EUConsumerSalesMethod.{FixedEstablishment, DispatchWarehouse}
import pages.behaviours.PageBehaviours

class SellsGoodsToEUConsumerMethodPageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "SellsGoodsToEUConsumerMethodPage" - {

    beRetrievable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beSettable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beRemovable[EUConsumerSalesMethod](SellsGoodsToEUConsumerMethodPage(countryIndex))

    "must navigate" - {

      "to Cannot Add Country when user answers Fixed Establishment" in {

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(FixedEstablishment)
          .mustEqual(controllers.euDetails.routes.CannotAddCountryController.onPageLoad())
      }

      "to ??? when user answers Dispatch Warehouse" in {
        //TODO
//        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(DispatchWarehouse)
//          .mustEqual(???)
      }

    }
  }
}
