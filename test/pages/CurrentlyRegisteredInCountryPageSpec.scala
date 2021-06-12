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
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.routes
import models.{CheckMode, CurrentlyRegisteredInCountry, NormalMode}
import pages.behaviours.PageBehaviours

class CurrentlyRegisteredInCountryPageSpec extends SpecBase with PageBehaviours {

  "CurrentlyRegisteredInCountryPage" - {

    beRetrievable[CurrentlyRegisteredInCountry](CurrentlyRegisteredInCountryPage)

    beSettable[CurrentlyRegisteredInCountry](CurrentlyRegisteredInCountryPage)

    beRemovable[CurrentlyRegisteredInCountry](CurrentlyRegisteredInCountryPage)

    "must navigate in Normal mode" - {

      "to Previously Registered" in {

        CurrentlyRegisteredInCountryPage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Check Your Answers" in {

        CurrentlyRegisteredInCountryPage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}
