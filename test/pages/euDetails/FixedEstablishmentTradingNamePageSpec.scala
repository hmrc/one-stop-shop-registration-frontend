/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{Index, NormalMode}
import pages.behaviours.PageBehaviours
import pages.euDetails

class FixedEstablishmentTradingNamePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "FixedEstablishmentTradingNamePage" - {

    beRetrievable[String](FixedEstablishmentTradingNamePage(index))

    beSettable[String](euDetails.FixedEstablishmentTradingNamePage(index))

    beRemovable[String](euDetails.FixedEstablishmentTradingNamePage(index))

    "must navigate in Normal mode" - {

      "to Fixed Establishment Address" in {

        FixedEstablishmentTradingNamePage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.FixedEstablishmentAddressController.onPageLoad(NormalMode, index))
      }
    }
  }
}
