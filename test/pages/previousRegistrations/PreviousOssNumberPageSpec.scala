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

package pages.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRegRoutes}
import models.{CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class PreviousOssNumberPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousEuVatNumberPage" - {

    beRetrievable[String](PreviousOssNumberPage(index))

    beSettable[String](PreviousOssNumberPage(index))

    beRemovable[String](PreviousOssNumberPage(index))

    "must navigate in Normal mode" - {

      "to Add Previous Registration" in {

        PreviousOssNumberPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "to Add Previous Registration when the VAT number for this index has been answered" in {

        val answers = emptyUserAnswers.set(PreviousOssNumberPage(index), "123").success.value
        PreviousOssNumberPage(index).navigate(CheckMode, answers)
          .mustEqual(prevRegRoutes.CheckPreviousSchemeAnswersController.onPageLoad(CheckMode, index))
      }
    }
  }
}
