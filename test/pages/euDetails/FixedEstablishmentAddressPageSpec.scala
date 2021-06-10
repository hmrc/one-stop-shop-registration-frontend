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
import models.{CheckLoopMode, CheckMode, Index, NormalMode}
import models.euDetails.FixedEstablishmentAddress
import pages.behaviours.PageBehaviours
import pages.euDetails

class FixedEstablishmentAddressPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "FixedEstablishmentAddressPage" - {

    beRetrievable[FixedEstablishmentAddress](FixedEstablishmentAddressPage(index))

    beSettable[FixedEstablishmentAddress](euDetails.FixedEstablishmentAddressPage(index))

    beRemovable[FixedEstablishmentAddress](euDetails.FixedEstablishmentAddressPage(index))

    "must navigate in Normal mode" - {

      "to Check Eu Details Answers" in {

        FixedEstablishmentAddressPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
      }
    }

    // TODO: This page needs a mode adding!!
    "must navigate in Check mode" - {

      "to Check Eu Details Answers" in {

        FixedEstablishmentAddressPage(index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
      }
    }

    "must navigate in Check Loop mode" - {

      "to Check Eu Details Answers in Normal mode" in {

        FixedEstablishmentAddressPage(index).navigate(CheckLoopMode, emptyUserAnswers)
          .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
      }
    }
  }
}
