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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.routes as euRoutes
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import controllers.routes
import models.{AmendMode, CheckMode, Country, Index, NormalMode, RejoinMode}
import pages.behaviours.PageBehaviours

class AddEuDetailsPageSpec extends SpecBase with PageBehaviours {

  "AddAdditionalEuVatDetailsPage" - {

    beRetrievable[Boolean](AddEuDetailsPage)

    beSettable[Boolean](AddEuDetailsPage)

    beRemovable[Boolean](AddEuDetailsPage)
    
    "must navigate in Normal mode" - {
      
      "when the answer is yes" - {
        
        "to Eu Country with an Index(0) equal to the number of countries we have details for" in {
          
          val answers =
            emptyUserAnswers
              .set(AddEuDetailsPage, true).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
              
          AddEuDetailsPage.navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(NormalMode, Index(1)))
        }
      }

      "when the user answers no" - {

        "must be Is Online Marketplace" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value
          AddEuDetailsPage.navigate(NormalMode, answers)
            .mustEqual(routes.IsOnlineMarketplaceController.onPageLoad(NormalMode))
        }
      }

      "when the user answers empty" - {

        "must be Journey recovery" in {

          AddEuDetailsPage.navigate(NormalMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Check Mode" - {

      "when the answer is yes" - {

        "to Eu Country with an Index(0) equal to the number of countries we have details for" in {

          val answers =
            emptyUserAnswers
              .set(AddEuDetailsPage, true).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), true).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value

          AddEuDetailsPage.navigate(CheckMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(CheckMode, Index(1)))
        }
      }
      
      "when the answer is no" - {

        "to Check Your Answers" in {
            
          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value
                
          AddEuDetailsPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "when the user answers empty" - {

        "must be Journey recovery" in {

          AddEuDetailsPage.navigate(CheckMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Amend Mode" - {

      "when the answer is yes" - {

        "to Eu Country with an Index(0) equal to the number of countries we have details for" in {

          val answers =
            emptyUserAnswers
              .set(AddEuDetailsPage, true).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), true).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value

          AddEuDetailsPage.navigate(AmendMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(AmendMode, Index(1)))
        }
      }

      "when the answer is no" - {

        "to Change Your Registration" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value

          AddEuDetailsPage.navigate(AmendMode, answers)
            .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
        }
      }

      "when the user answers empty" - {

        "must be Amend Journey recovery" in {

          AddEuDetailsPage.navigate(AmendMode, emptyUserAnswers)
            .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must navigate in Rejoin Mode" - {

      "when the answer is yes" - {

        "to Eu Country with an Index(0) equal to the number of countries we have details for" in {

          val answers =
            emptyUserAnswers
              .set(AddEuDetailsPage, true).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), true).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value

          AddEuDetailsPage.navigate(RejoinMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(RejoinMode, Index(1)))
        }
      }

      "when the answer is no" - {

        "to Rejoin Registration" in {

          val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value

          AddEuDetailsPage.navigate(RejoinMode, answers)
            .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
        }
      }

      "when the user answers empty" - {

        "must be Rejoin Journey recovery" in {

          AddEuDetailsPage.navigate(RejoinMode, emptyUserAnswers)
            .mustEqual(rejoinRoutes.RejoinJourneyRecoveryController.onPageLoad())
        }
      }
    }
  }
}
