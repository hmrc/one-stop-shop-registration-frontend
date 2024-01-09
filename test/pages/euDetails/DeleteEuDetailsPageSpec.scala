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
import controllers.euDetails.{routes => euRoutes}
import models.{AmendMode, CheckMode, Country, Index, NormalMode}

class DeleteEuDetailsPageSpec extends SpecBase {

  "DeleteEuDetailsPage" - {

    "must navigate in Normal mode" - {

      "to Tax Registered in EU when there are no countries left in the user's answers" in {

        DeleteEuDetailsPage(Index(0)).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.TaxRegisteredInEuController.onPageLoad(NormalMode))
      }

      "to Add EU Details when we still have countries in the user's answers" in {

        val answers =
          emptyUserAnswers
            .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
            .set(pages.euDetails.EuVatNumberPage(Index(0)), "VAT Number").success.value

        DeleteEuDetailsPage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Tax Registered in EU when there are no countries left in the user's answers" in {

        DeleteEuDetailsPage(Index(0)).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(euRoutes.TaxRegisteredInEuController.onPageLoad(CheckMode))
      }

      "to Add EU Details when we still have countries in the user's answers" in {

        val answers =
          emptyUserAnswers
            .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
            .set(pages.euDetails.EuVatNumberPage(Index(0)), "VAT Number").success.value

        DeleteEuDetailsPage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(CheckMode))
      }
    }

    "must navigate in Amend mode" - {

      "to Tax Registered in EU when there are no countries left in the user's answers" in {

        DeleteEuDetailsPage(Index(0)).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(euRoutes.TaxRegisteredInEuController.onPageLoad(AmendMode))
      }

      "to Add EU Details when we still have countries in the user's answers" in {

        val answers =
          emptyUserAnswers
            .set(pages.euDetails.EuCountryPage(Index(0)), Country("FR", "France")).success.value
            .set(pages.euDetails.EuVatNumberPage(Index(0)), "VAT Number").success.value

        DeleteEuDetailsPage(Index(0)).navigate(AmendMode, answers)
          .mustEqual(euRoutes.AddEuDetailsController.onPageLoad(AmendMode))
      }
    }
  }
}
