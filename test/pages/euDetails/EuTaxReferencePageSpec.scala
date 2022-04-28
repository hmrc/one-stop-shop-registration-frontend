/*
 * Copyright 2022 HM Revenue & Customs
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

      "to Fixed Establishment Trading Name when Has Fixed Establishment is true" in {

        EuTaxReferencePage(index).navigate(NormalMode, emptyUserAnswers.set(HasFixedEstablishmentPage(index), true).success.value)
          .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
      }

      "to Eu Send Goods Trading Name when Has Fixed Establishment is false" in {

        EuTaxReferencePage(index).navigate(NormalMode, emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value)
          .mustEqual(euRoutes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "when Has Fixed Establishment has not been answered" - {

        "to Has Fixed Establishment" in {

          EuTaxReferencePage(index).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckMode, index))
        }
      }

      "when Has Fixed Establishment has been answered" - {

        "to wherever Has Fixed Establishment would navigate to" in {

          val answers = emptyUserAnswers
            .set(EuTaxReferencePage(index), "123").success.value
            .set(HasFixedEstablishmentPage(index), true).success.value

          EuTaxReferencePage(index).navigate(CheckMode, answers)
            .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckMode, answers))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when Has Fixed Establishment has not been answered" - {

        "to Has Fixed Establishment" in {

          EuTaxReferencePage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(CheckLoopMode, index))
        }
      }

      "when Has Fixed Establishment has been answered" - {

        "to wherever Has Fixed Establishment would navigate to" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), true).success.value

          EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
            .mustEqual(HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers))
        }
      }
    }
  }
}
