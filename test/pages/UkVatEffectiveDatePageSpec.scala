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
import models.NormalMode

import java.time.LocalDate
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

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

      "to Business Address in UK if we don't have the user's VAT info" in {

        UkVatEffectiveDatePage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.BusinessAddressInUkController.onPageLoad(NormalMode))
      }
    }
  }
}
