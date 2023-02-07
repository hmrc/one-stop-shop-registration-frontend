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

import controllers.euDetails.{routes => euRoutes}
import base.SpecBase
import models.{Index, NormalMode}
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

      "to No Sales of Services Info when answer is No" in {

        //TODO
      }

    }

    "must navigate in Check Mode" - {

      //TODO

    }
  }
}
