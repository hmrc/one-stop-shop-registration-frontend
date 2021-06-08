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

class VatRegisteredPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "VatRegisteredInEuPage" - {

    beRetrievable[Boolean](VatRegisteredPage(index))

    beSettable[Boolean](VatRegisteredPage(index))

    beRemovable[Boolean](VatRegisteredPage(index))

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to EU VAT Number for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), true).success.value

          VatRegisteredPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuVatNumberController.onPageLoad(NormalMode, index))
        }
      }

      "when the answer is no" - {

        "to Has Fixed Establishment for the same index" in {

          val answers = emptyUserAnswers.set(VatRegisteredPage(index), false).success.value

          VatRegisteredPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.HasFixedEstablishmentController.onPageLoad(NormalMode, index))
        }
      }
    }
  }
}
