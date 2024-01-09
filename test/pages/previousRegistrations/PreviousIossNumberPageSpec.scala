/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.previousRegistrations.routes
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class PreviousIossNumberPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousIossNumberPage" - {

    beRetrievable[PreviousSchemeNumbers](PreviousIossNumberPage(index, index))

    beSettable[PreviousSchemeNumbers](PreviousIossNumberPage(index, index))

    beRemovable[PreviousSchemeNumbers](PreviousIossNumberPage(index, index))

    "must navigate in Normal mode" - {

      "to Check Previous Scheme Answers Page" in {

        PreviousIossNumberPage(index, index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.CheckPreviousSchemeAnswersController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "to Check Previous Scheme Answers Page" in {

        PreviousIossNumberPage(index, index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckPreviousSchemeAnswersController.onPageLoad(CheckMode, index))
      }
    }

    "must navigate in Amend mode" - {

      "to Check Previous Scheme Answers Page" in {

        PreviousIossNumberPage(index, index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(routes.CheckPreviousSchemeAnswersController.onPageLoad(AmendMode, index))
      }
    }

  }
}
