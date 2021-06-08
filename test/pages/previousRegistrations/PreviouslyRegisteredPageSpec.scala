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

package pages.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.routes
import models.{Index, NormalMode}
import pages.behaviours.PageBehaviours

class PreviouslyRegisteredPageSpec extends SpecBase with PageBehaviours {

  "PreviouslyRegisteredPage" - {

    beRetrievable[Boolean](PreviouslyRegisteredPage)

    beSettable[Boolean](PreviouslyRegisteredPage)

    beRemovable[Boolean](PreviouslyRegisteredPage)

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to Previous EU Country for index 0" in {

          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, true).success.value

          PreviouslyRegisteredPage.navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(0)))
        }
      }

      "when the answer is no" - {

        "to Start Date" in {

          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, false).success.value

          PreviouslyRegisteredPage.navigate(NormalMode, answers)
            .mustEqual(routes.StartDateController.onPageLoad(NormalMode))
        }
      }
    }
  }
}
