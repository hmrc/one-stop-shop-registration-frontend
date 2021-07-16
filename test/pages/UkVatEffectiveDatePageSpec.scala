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
import controllers.routes
import models.{CheckMode, NormalMode}
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class UkVatEffectiveDatePageSpec extends SpecBase with PageBehaviours {

  "UkVatEffectiveDatePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](UkVatEffectiveDatePage)

    beSettable[LocalDate](UkVatEffectiveDatePage)

    beRemovable[LocalDate](UkVatEffectiveDatePage)

    "must navigate in Normal mode" - {

      "to Has Trading Name if we have the user's VAT info" in {

        UkVatEffectiveDatePage.navigate(NormalMode, emptyUserAnswersWithVatInfo)
          .mustEqual(routes.HasTradingNameController.onPageLoad(NormalMode))
      }

      "to UK Business Address if user answers yes to Business Based in Northern Ireland with no VAT info" in {

        val answers = emptyUserAnswers.set(BusinessBasedInNiPage, true).success.value

        UkVatEffectiveDatePage.navigate(NormalMode, answers)
          .mustEqual(routes.UkAddressController.onPageLoad(NormalMode))
      }

      "to Is Your Principal Place of Business in Great Britain if user answers no to Business Based in Northern Ireland with no VAT info" in {

        val answers = emptyUserAnswers.set(BusinessBasedInNiPage, false).success.value

        UkVatEffectiveDatePage.navigate(NormalMode, answers)
          .mustEqual(routes.BusinessAddressInUkController.onPageLoad(NormalMode))
      }

      "to Has Trading Name if user has partial VAT info including address" in {

        UkVatEffectiveDatePage.navigate(NormalMode, partialUserAnswersWithVatInfo)
          .mustEqual(routes.HasTradingNameController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Check Your Answers" in {

        UkVatEffectiveDatePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }
    }
  }
}
