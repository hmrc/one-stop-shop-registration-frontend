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


class RegisteredCompanyNamePageSpec extends SpecBase with PageBehaviours {

  "RegisteredCompanyNamePage" - {

    beRetrievable[String](RegisteredCompanyNamePage)

    beSettable[String](RegisteredCompanyNamePage)

    beRemovable[String](RegisteredCompanyNamePage)

    "must navigate in Normal mode" - {

      "when we have VAT details including Part of VAT Group" - {

        "to wherever the Part of VAT Group page would navigate to" in {

          RegisteredCompanyNamePage.navigate(NormalMode, emptyUserAnswersWithVatInfo)
            .mustBe(PartOfVatGroupPage.navigate(NormalMode, emptyUserAnswersWithVatInfo))
        }
      }

      "when we have VAT details that don't include Part of VAT Group" - {

        "to Part of VAT Group" in {

          val vatInfo = vatCustomerInfo copy (partOfVatGroup = None)
          val answers = emptyUserAnswers copy (vatInfo = Some(vatInfo))

          RegisteredCompanyNamePage.navigate(NormalMode, answers)
            .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
        }
      }

      "when we don't have VAT details" - {

        "to Part of VAT Group" in {

          RegisteredCompanyNamePage.navigate(NormalMode, emptyUserAnswers)
            .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
        }
      }
    }
  }
}
