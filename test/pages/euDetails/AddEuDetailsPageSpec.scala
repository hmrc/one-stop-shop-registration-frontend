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
import models.{Country, Index, NormalMode}
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
              .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(pages.euDetails.EuVatNumberPage(Index(0)), "FR123456789").success.value
              
          AddEuDetailsPage.navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(NormalMode, Index(1)))
        }
      }

      "when the user answers no" - {

        "and has entered one country with a VAT registration" - {

          "must be Currently Registered in Country" in {

            val answers = emptyUserAnswers
              .set(AddEuDetailsPage, false).success.value
              .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(pages.euDetails.VatRegisteredPage(Index(0)), true).success.value
              .set(pages.euDetails.EuVatNumberPage(Index(0)), "FR123456789").success.value
              .set(pages.euDetails.HasFixedEstablishmentPage(Index(0)), false).success.value

            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustEqual(routes.CurrentlyRegisteredInCountryController.onPageLoad(NormalMode))
          }
        }

        "and has entered multiple countries with VAT registrations" - {

          "must be Currently Registered in EU" in {
            val answers = emptyUserAnswers
              .set(AddEuDetailsPage, false).success.value
              .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(pages.euDetails.VatRegisteredPage(Index(0)), true).success.value
              .set(pages.euDetails.EuVatNumberPage(Index(0)), "FR123456789").success.value
              .set(pages.euDetails.HasFixedEstablishmentPage(Index(0)), false).success.value
              .set(pages.euDetails.EuCountryPage(Index(1)), Country("DE", "Germany")).success.value
              .set(pages.euDetails.VatRegisteredPage(Index(1)), true).success.value
              .set(pages.euDetails.EuVatNumberPage(Index(1)), "DE123456789").success.value
              .set(pages.euDetails.HasFixedEstablishmentPage(Index(1)), false).success.value

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
              .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
              .set(pages.euDetails.VatRegisteredPage(Index(0)), false).success.value

            AddEuDetailsPage.navigate(NormalMode, answers)
              .mustBe(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
          }
        }
      }
    }
  }
}
