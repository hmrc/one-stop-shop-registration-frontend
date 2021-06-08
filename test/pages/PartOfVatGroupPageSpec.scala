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

package pages

import base.SpecBase
import controllers.routes
import models.NormalMode
import pages.behaviours.PageBehaviours

class PartOfVatGroupPageSpec extends SpecBase with PageBehaviours {

  "PartOfVatGroupPage" - {

    beRetrievable[Boolean](PartOfVatGroupPage)

    beSettable[Boolean](PartOfVatGroupPage)

    beRemovable[Boolean](PartOfVatGroupPage)
    
    "must navigate in Normal mode" - {

      "when we have VAT details including Registration Date" - {

        "to wherever the UK VAT Effective Date page would navigate to" in {

          PartOfVatGroupPage.navigate(NormalMode, emptyUserAnswersWithVatInfo)
            .mustBe(UkVatEffectiveDatePage.navigate(NormalMode, emptyUserAnswersWithVatInfo))
        }
      }

      "when we have VAT details that don't include Registration Date" - {

        "to UK VAT Effective Date" in {

          val vatInfo = vatCustomerInfo copy (registrationDate  = None)
          val answers = emptyUserAnswers copy (vatInfo = Some(vatInfo))

          PartOfVatGroupPage.navigate(NormalMode, answers)
            .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
        }
      }

      "when we don't have VAT details" - {

        "to UK VAT Effective Date" in {

          PartOfVatGroupPage.navigate(NormalMode, emptyUserAnswers)
            .mustBe(routes.UkVatEffectiveDateController.onPageLoad(NormalMode))
        }
      }
    }
  }
}
