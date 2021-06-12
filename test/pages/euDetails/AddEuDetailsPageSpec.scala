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

package pages.euDetails

import controllers.euDetails.{routes => euRoutes}
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.routes
import base.SpecBase
import models.CurrentlyRegisteredInCountry.No
import models.{CheckMode, Country, Index, NormalMode}
import pages.{CurrentlyRegisteredInCountryPage, CurrentlyRegisteredInEuPage}
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

        "and has entered one country with a VAT registration" - {

          "must be Currently Registered in Country" in {

            val answers = emptyUserAnswers
              .set(AddEuDetailsPage, false).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), true).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
              .set(HasFixedEstablishmentPage(Index(0)), false).success.value

            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustEqual(routes.CurrentlyRegisteredInCountryController.onPageLoad(NormalMode))
          }
        }

        "and has entered multiple countries with VAT registrations" - {

          "must be Currently Registered in EU" in {
            val answers = emptyUserAnswers
              .set(AddEuDetailsPage, false).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), true).success.value
              .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
              .set(HasFixedEstablishmentPage(Index(0)), false).success.value
              .set(EuCountryPage(Index(1)), Country("DE", "Germany")).success.value
              .set(VatRegisteredPage(Index(1)), true).success.value
              .set(EuVatNumberPage(Index(1)), "DE123456789").success.value
              .set(HasFixedEstablishmentPage(Index(1)), false).success.value

            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustEqual(routes.CurrentlyRegisteredInEuController.onPageLoad(NormalMode))
          }
        }

        "and has entered no countries" - {

          "must be Previously Registered" in {

            val answers = emptyUserAnswers.set(AddEuDetailsPage, false).success.value
            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
          }
        }

        "and has entered only countries without VAT registrations" - {

          "must be Previously Registered" in {

            val answers = emptyUserAnswers
              .set(AddEuDetailsPage, false).success.value
              .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(VatRegisteredPage(Index(0)), false).success.value
              .set(HasFixedEstablishmentPage(Index(0)), false).success.value

            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustBe(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
          }
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
              .set(HasFixedEstablishmentPage(Index(0)), false).success.value

          AddEuDetailsPage.navigate(CheckMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(CheckMode, Index(1)))
        }
      }
      
      "when the answer is no" - {
        
        "and there are no VAT registered countries in the user's answers" - {
          
          "to Check Your Answers" in {
            
            val answers =
              emptyUserAnswers
                .set(AddEuDetailsPage, false).success.value
                .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                .set(VatRegisteredPage(Index(0)), false).success.value
                .set(HasFixedEstablishmentPage(Index(0)), false).success.value
                
            AddEuDetailsPage.navigate(CheckMode, answers)
              .mustEqual(routes.CheckYourAnswersController.onPageLoad())
          }
        }

        "and there is one VAT registered country in the user's answers" - {

          "and the user has answered Currently Registered in Country" - {

            "to Check Your Answers" in {

              val answers =
                emptyUserAnswers
                  .set(AddEuDetailsPage, false).success.value
                  .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                  .set(VatRegisteredPage(Index(0)), true).success.value
                  .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(0)), false).success.value
                  .set(CurrentlyRegisteredInCountryPage, No).success.value

              AddEuDetailsPage.navigate(CheckMode, answers)
                .mustEqual(routes.CheckYourAnswersController.onPageLoad())
            }
          }

          "and the user has not answered Currently Registered in Country" - {

            "to Currently Registered in Country" in {

              val answers =
                emptyUserAnswers
                  .set(AddEuDetailsPage, false).success.value
                  .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                  .set(VatRegisteredPage(Index(0)), true).success.value
                  .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(0)), false).success.value

              AddEuDetailsPage.navigate(CheckMode, answers)
                .mustEqual(routes.CurrentlyRegisteredInCountryController.onPageLoad(CheckMode))
            }
          }
        }

        "and there are two or more VAT registered countries in the user's answers" - {

          "and the user has answered Currently Registered in EU" - {

            "to Check Your Answers" in {

              val answers =
                emptyUserAnswers
                  .set(AddEuDetailsPage, false).success.value
                  .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                  .set(VatRegisteredPage(Index(0)), true).success.value
                  .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(0)), false).success.value
                  .set(EuCountryPage(Index(1)), Country("ES", "Spain")).success.value
                  .set(VatRegisteredPage(Index(1)), true).success.value
                  .set(EuVatNumberPage(Index(1)), "ES123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(1)), false).success.value
                  .set(CurrentlyRegisteredInEuPage, true).success.value

              AddEuDetailsPage.navigate(CheckMode, answers)
                .mustEqual(routes.CheckYourAnswersController.onPageLoad())
            }
          }

          "and the user has not answered Currently Registered in EU" - {

            "to Currently Registered in EU" in {

              val answers =
                emptyUserAnswers
                  .set(AddEuDetailsPage, false).success.value
                  .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                  .set(VatRegisteredPage(Index(0)), true).success.value
                  .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(0)), false).success.value
                  .set(EuCountryPage(Index(1)), Country("ES", "Spain")).success.value
                  .set(VatRegisteredPage(Index(1)), true).success.value
                  .set(EuVatNumberPage(Index(1)), "ES123456789").success.value
                  .set(HasFixedEstablishmentPage(Index(1)), false).success.value

              AddEuDetailsPage.navigate(CheckMode, answers)
                .mustEqual(routes.CurrentlyRegisteredInEuController.onPageLoad(CheckMode))
            }
          }
        }
      }
    }
  }
}
