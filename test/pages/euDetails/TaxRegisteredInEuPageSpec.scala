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

import base.SpecBase
import controllers.euDetails.{routes => euRoutes}
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.routes
import models.{CheckMode, Country, Index, NormalMode, UserAnswers}
import pages.behaviours.PageBehaviours

class TaxRegisteredInEuPageSpec extends SpecBase with PageBehaviours {

  "TaxRegisteredInEuPage" - {

    beRetrievable[Boolean](TaxRegisteredInEuPage)

    beSettable[Boolean](TaxRegisteredInEuPage)

    beRemovable[Boolean](TaxRegisteredInEuPage)

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "to EU Country for index 0" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value

          TaxRegisteredInEuPage.navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuCountryController.onPageLoad(NormalMode, Index(0)))
        }
      }

      "when the answer is no" - {

        "to Previously Registered" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          TaxRegisteredInEuPage.navigate(NormalMode, answers)
            .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "and country details have already been given" - {

          "to Check Your Answers" in {

            val answers =
              emptyUserAnswers
                .set(TaxRegisteredInEuPage, true).success.value
                .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
                .set(VatRegisteredPage(Index(0)), false).success.value
                .set(HasFixedEstablishmentPage(Index(0)), false).success.value

            TaxRegisteredInEuPage.navigate(CheckMode, answers)
              .mustEqual(routes.CheckYourAnswersController.onPageLoad())
          }
        }

        "and no country details have already been given" - {

          "to EU Country (index 0)" in {

            val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value

            TaxRegisteredInEuPage.navigate(CheckMode, answers)
              .mustEqual(euRoutes.EuCountryController.onPageLoad(CheckMode, Index(0)))
          }
        }
      }

      "when the answer is no" - {

        "to Check Your Answers" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          TaxRegisteredInEuPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }
    }

    "must remove all EU VAT details when the answer is false" in {

      val answers =
        UserAnswers("id")
          .set(EuCountryPage(Index(0)), Country.euCountries.head).success.value
          .set(VatRegisteredPage(Index(0)), true).success.value
          .set(EuVatNumberPage(Index(0)), "reg 1").success.value
          .set(HasFixedEstablishmentPage(Index(0)), false).success.value
          .set(EuCountryPage(Index(1)), Country.euCountries.tail.head).success.value
          .set(EuVatNumberPage(Index(1)), "reg 2").success.value

      val result = answers.set(TaxRegisteredInEuPage, false).success.value

      result.get(EuCountryPage(Index(0))) must not be defined
      result.get(VatRegisteredPage(Index(0))) must not be defined
      result.get(EuVatNumberPage(Index(0))) must not be defined
      result.get(HasFixedEstablishmentPage(Index(0))) must not be defined
      result.get(EuCountryPage(Index(1))) must not be defined
      result.get(EuVatNumberPage(Index(1))) must not be defined
    }

    "must not remove any EU VAT details when the answer is true" in {

      val answers =
        UserAnswers("id")
          .set(EuCountryPage(Index(0)), Country.euCountries.head).success.value
          .set(EuVatNumberPage(Index(0)), "reg 1").success.value
          .set(EuCountryPage(Index(1)), Country.euCountries.tail.head).success.value
          .set(EuVatNumberPage(Index(1)), "reg 2").success.value

      val result = answers.set(TaxRegisteredInEuPage, true).success.value

      result.get(EuCountryPage(Index(0))).value mustEqual Country.euCountries.head
      result.get(EuVatNumberPage(Index(0))).value mustEqual "reg 1"
      result.get(EuCountryPage(Index(1))).value mustEqual Country.euCountries.tail.head
      result.get(EuVatNumberPage(Index(1))).value mustEqual "reg 2"
    }
  }
}
