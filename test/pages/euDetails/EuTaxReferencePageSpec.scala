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
import pages.behaviours.PageBehaviours

class EuTaxReferencePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "EuTaxReferencePage" - {

    beRetrievable[String](EuTaxReferencePage(index))

    beSettable[String](EuTaxReferencePage(index))

    beRemovable[String](EuTaxReferencePage(index))

    "must navigate in Normal mode" - {

      "to Fixed Establishment Trading Name" in {

        EuTaxReferencePage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "when Fixed Establishment Trading Name has not been answered" - {

        "to Fixed Establishment Trading Name" in {

          EuTaxReferencePage(index).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, index))
        }
      }

      "when Fixed Establishment Trading Name has been answered" - {

        "to wherever Fixed Establishment Trading Name would navigate to" in {

          val answers = emptyUserAnswers.set(FixedEstablishmentTradingNamePage(index), "foo").success.value

          EuTaxReferencePage(index).navigate(CheckMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when Fixed Establishment Trading Name has not been answered" - {

        "to Fixed Establishment Trading Name" in {

          EuTaxReferencePage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, index))
        }
      }

      "when Fixed Establishment Trading Name has been answered" - {

        "to wherever Fixed Establishment Trading Name would navigate to" in {

          val answers = emptyUserAnswers.set(FixedEstablishmentTradingNamePage(index), "foo").success.value

          EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
            .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers))
        }
      }
    }
  }
}
