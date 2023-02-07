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
import models.{Index, NormalMode}
import pages.behaviours.PageBehaviours

class SellsGoodsToEUConsumerMethodPageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "SellsGoodsToEUConsumerMethodPage" - {

    beRetrievable[Boolean](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beSettable[Boolean](SellsGoodsToEUConsumerMethodPage(countryIndex))

    beRemovable[Boolean](SellsGoodsToEUConsumerMethodPage(countryIndex))

    "must navigate in Normal Mode" - {

      "to Cannot Add Country when user answers Fixed Establishment" in {

        val answers = emptyUserAnswers.set(SellsGoodsToEUConsumerMethodPage(countryIndex), true).success.value

        SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, answers)
          .mustEqual(controllers.euDetails.routes.CannotAddCountryController.onPageLoad())
      }

      "to Cannot Add Country when user answers Dispatch Warehouse" in {

        //TODO
      }

    }

    "must navigate in Check Mode" - {

      //TODO
    }
  }
}
