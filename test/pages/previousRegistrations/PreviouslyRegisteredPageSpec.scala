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
import controllers.routes
import models.{CheckMode, Country, Index, NormalMode}
import models.previousRegistrations.PreviousSchemeNumbers
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

        "to Is Online Marketplace" in {

          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, false).success.value

          PreviouslyRegisteredPage.navigate(NormalMode, answers)
            .mustEqual(routes.IsOnlineMarketplaceController.onPageLoad(NormalMode))
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          PreviouslyRegisteredPage.navigate(NormalMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "and there are already some previous registrations in the user's answers" - {

          "to Check Your Answers" in {

            val answers =
              emptyUserAnswers
                .set(PreviouslyRegisteredPage, true).success.value
                .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
                .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("123", None)).success.value

            PreviouslyRegisteredPage.navigate(CheckMode, answers)
              .mustEqual(routes.CheckYourAnswersController.onPageLoad())
          }
        }

        "and there are no previous registrations in the user's answers" - {

          "to Previous EU Country with index 0" in {

            val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, true).success.value

            PreviouslyRegisteredPage.navigate(CheckMode, answers)
              .mustEqual(prevRegRoutes.PreviousEuCountryController.onPageLoad(CheckMode, Index(0)))
          }
        }
      }

      "when the answer is no" - {

        "to Check Your Answers" in {

          val answers = emptyUserAnswers.set(PreviouslyRegisteredPage, false).success.value

          PreviouslyRegisteredPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "when the answer is empty" - {

        "to Journey recovery" in {

          PreviouslyRegisteredPage.navigate(CheckMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must remove all previous registrations when the answer is no" in {

      val answers =
        emptyUserAnswers
          .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("123", None)).success.value

      val result = answers.set(PreviouslyRegisteredPage, false).success.value

      result.get(PreviousEuCountryPage(Index(0))) must not be defined
      result.get(PreviousOssNumberPage(Index(0), Index(0))) must not be defined
    }

    "must leave all previous registrations in place when the answer is yes" in {

      val answers =
        emptyUserAnswers
          .set(PreviousEuCountryPage(Index(0)), Country("FR", "France")).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers("123", None)).success.value

      val result = answers.set(PreviouslyRegisteredPage, true).success.value

      result.get(PreviousEuCountryPage(Index(0))).value mustEqual Country("FR", "France")
      result.get(PreviousOssNumberPage(Index(0), Index(0))).value mustEqual PreviousSchemeNumbers("123", None)
    }
  }
}
