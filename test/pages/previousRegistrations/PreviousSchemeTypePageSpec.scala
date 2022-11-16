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
import models.{Index, NormalMode, PreviousSchemeType}
import pages.behaviours.PageBehaviours

class PreviousSchemeTypePageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousSchemePage" - {

    beRetrievable[PreviousSchemeType](PreviousSchemeTypePage(index))

    beSettable[PreviousSchemeType](PreviousSchemeTypePage(index))

    beRemovable[PreviousSchemeType](PreviousSchemeTypePage(index))

    "must navigate in Normal mode" - {

      "to OSS scheme number when OSS is selected" in {

        PreviousOssNumberPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(NormalMode))
      }

      "to IOSS scheme when IOSS is selected" in {

        PreviousOssNumberPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(controllers.previousRegistrations.routes.PreviousIossSchemeController.onPageLoad(NormalMode))
      }
    }
  }
}
