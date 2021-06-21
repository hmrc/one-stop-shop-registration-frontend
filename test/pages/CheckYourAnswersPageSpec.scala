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

class CheckYourAnswersPageSpec extends SpecBase {

  "CheckYourAnswersPage" - {

    "must navigate in Normal mode" - {

      "to Application Complete with email confirmation" in {

        CheckYourAnswersPage.navigateWithEmailConfirmation(true)
          .mustBe(routes.ApplicationCompleteController.onPageLoad(true))
      }

      "to Application Complete without email confirmation" in {

        CheckYourAnswersPage.navigateWithEmailConfirmation(false)
          .mustBe(routes.ApplicationCompleteController.onPageLoad(false))
      }
    }
  }
}
