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
import models.{AmendMode, CheckMode, Country, Index, NormalMode}
import models.previousRegistrations.PreviousSchemeNumbers

class DeletePreviousRegistrationPageSpec extends SpecBase {

  "DeletePreviousRegistrationPage" - {

    "must navigate in Normal mode" - {

      "when there are still some previous registrations" - {

        "to Add Previous Registration" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

          DeletePreviousRegistrationPage(Index(0)).navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(NormalMode))
        }
      }


      "when there are no previous registrations left" - {

        "to Previously Registered" in {

          DeletePreviousRegistrationPage(Index(0)).navigate(NormalMode, emptyUserAnswers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }
    }

    "must navigate in Check mode" - {

      "when there are still some previous registrations" - {

        "to Add Previous Registration" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

          DeletePreviousRegistrationPage(Index(0)).navigate(CheckMode, answers)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(CheckMode))
        }
      }


      "when there are no previous registrations left" - {

        "to Previously Registered" in {

          DeletePreviousRegistrationPage(Index(0)).navigate(CheckMode, emptyUserAnswers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(CheckMode))
        }
      }
    }

    "must navigate in Amend mode" - {

      "when there are still some previous registrations" - {

        "to Add Previous Registration" in {

          val answers =
            emptyUserAnswers
              .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("FR123", None)).success.value

          DeletePreviousRegistrationPage(Index(0)).navigate(AmendMode, answers)
            .mustEqual(prevRegRoutes.AddPreviousRegistrationController.onPageLoad(AmendMode))
        }
      }


      "when there are no previous registrations left" - {

        "to Previously Registered" in {

          DeletePreviousRegistrationPage(Index(0)).navigate(AmendMode, emptyUserAnswers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(AmendMode))
        }
      }
    }

  }
}
