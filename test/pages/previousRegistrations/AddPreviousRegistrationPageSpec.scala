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
import models.{Country, Index, NormalMode}
import pages.behaviours.PageBehaviours

class AddPreviousRegistrationPageSpec extends SpecBase with PageBehaviours {

  "AddPreviousRegistrationPage" - {

    beRetrievable[Boolean](AddPreviousRegistrationPage)

    beSettable[Boolean](AddPreviousRegistrationPage)

    beRemovable[Boolean](AddPreviousRegistrationPage)

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to Previous EU Country with an index equal to the number of previous registrations already answered" in {

          val answers =
            emptyUserAnswers
              .set(PreviouslyRegisteredPage, true).success.value
              .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(PreviousEuVatNumberPage(Index(0)), "FR123").success.value
              .set(AddPreviousRegistrationPage, true).success.value

          AddPreviousRegistrationPage.navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.PreviousEuCountryController.onPageLoad(NormalMode, Index(1)))
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
