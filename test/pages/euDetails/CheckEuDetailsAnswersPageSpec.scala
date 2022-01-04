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
import models.{CheckLoopMode, CheckMode, NormalMode}

class CheckEuDetailsAnswersPageSpec extends SpecBase {

  "CheckEuDetailsAnswersPage" - {

    "must navigate in Normal mode" - {

      "to Add EU Details in Normal mode" in {
        CheckEuDetailsAnswersPage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add EU Details in Check mode" - {
        CheckEuDetailsAnswersPage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(CheckMode))
      }
    }

    "must navigate in Check Loop mode" - {

      "to Add EU Details in Normal mode (because the user has now finished checking this country)" in {
        CheckEuDetailsAnswersPage.navigate(CheckLoopMode, emptyUserAnswers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(NormalMode))
      }
    }
  }
}
