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

package pages.euDetails

import base.SpecBase
import controllers.euDetails.{routes => euRoutes}
import controllers.routes
import models.{CheckMode, Country, Index, NormalMode}
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

        "to Is Online Marketplace" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          TaxRegisteredInEuPage.navigate(NormalMode, answers)
            .mustEqual(routes.IsOnlineMarketplaceController.onPageLoad(NormalMode))
        }
      }

      "when the answer is empty" - {

        "to Previously Registered" in {

          TaxRegisteredInEuPage.navigate(NormalMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
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
                .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
                .set(VatRegisteredPage(Index(0)), false).success.value

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

        "to Delete All EU details if there are eu details in the user's answers" in {

          val answers = emptyUserAnswers
            .set(EuCountryPage(Index(0)), Country("DE", "Germany")).success.value
            .set(SellsGoodsToEUConsumersPage(Index(0)), false).success.value
            .set(VatRegisteredPage(Index(0)), true).success.value
            .set(EuVatNumberPage(Index(0)), "DE123456789").success.value
            .set(TaxRegisteredInEuPage, false).success.value

          TaxRegisteredInEuPage.navigate(CheckMode, answers)
            .mustEqual(euRoutes.DeleteAllEuDetailsController.onPageLoad())
        }

        "to Check Your Answers if there are not eu details in the user's answers" in {

          val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, false).success.value

          TaxRegisteredInEuPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }

        "to Journey recovery when the answer is empty" in {

          TaxRegisteredInEuPage.navigate(CheckMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }

      "when the answer is empty" - {

        "to Previously Registered" in {

          TaxRegisteredInEuPage.navigate(CheckMode, emptyUserAnswers)
            .mustEqual(routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }
  }
}
