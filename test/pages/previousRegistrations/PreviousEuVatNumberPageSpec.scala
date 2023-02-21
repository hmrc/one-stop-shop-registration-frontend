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

package pages.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRegRoutes}
import models.{CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class PreviousEuVatNumberPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousEuVatNumberPage" - {

    beRetrievable[String](PreviousEuVatNumberPage(index))

    beSettable[String](PreviousEuVatNumberPage(index))

    beRemovable[String](PreviousEuVatNumberPage(index))

    "must navigate in Normal mode" - {

      "to Add Previous Registration" in {

        PreviousEuVatNumberPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add Previous Registration when the VAT number for this index has been answered" in {

        val answers = emptyUserAnswers.set(PreviousEuVatNumberPage(index), "123").success.value
        PreviousEuVatNumberPage(index).navigate(CheckMode, answers)
          .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(CheckMode))
      }
    }
  }
}
