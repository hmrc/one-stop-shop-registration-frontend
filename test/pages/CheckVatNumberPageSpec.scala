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

package pages

import base.SpecBase
import controllers.routes
import models.NormalMode
import pages.behaviours.PageBehaviours

class CheckVatNumberPageSpec extends SpecBase with PageBehaviours {

  "CheckVatNumberPage" - {

    beRetrievable[Boolean](CheckVatNumberPage)

    beSettable[Boolean](CheckVatNumberPage)

    beRemovable[Boolean](CheckVatNumberPage)

    "must navigate in Normal mode" - {

      "to Registered Company Name when the answer is yes" in {

        val answers = emptyUserAnswers.set(CheckVatNumberPage, true).success.value
        CheckVatNumberPage.navigate(NormalMode, answers)
          .mustBe(routes.RegisteredCompanyNameController.onPageLoad(NormalMode))
      }

      "to Use Other Account when the answer is no" in {

        val answers = emptyUserAnswers.set(CheckVatNumberPage, false).success.value
        CheckVatNumberPage.navigate(NormalMode, answers)
          .mustBe(routes.UseOtherAccountController.onPageLoad())
      }
    }
  }
}
