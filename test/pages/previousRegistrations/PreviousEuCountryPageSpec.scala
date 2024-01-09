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

package pages.previousRegistrations

import base.SpecBase
import controllers.previousRegistrations.{routes => prevRegRoutes}
import models.domain.PreviousSchemeNumbers
import models.{AmendMode, CheckMode, Country, Index, NormalMode}
import pages.behaviours.PageBehaviours

class PreviousEuCountryPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "PreviousEuCountryPage" - {

    beRetrievable[Country](PreviousEuCountryPage(index))

    beSettable[Country](PreviousEuCountryPage(index))

    beRemovable[Country](PreviousEuCountryPage(index))

    "must navigate in Normal mode" - {

      "to Previous EU VAT Number for the same index" in {

        PreviousEuCountryPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(NormalMode, index, index))
      }
    }

    "must navigate in Check mode" - {

      "to Previous EU VAT number for the same index where the VAT number hasn't already been answered" in {

        PreviousEuCountryPage(index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(CheckMode, index, index))
      }

      "to Add Previous Registration when the VAT number for this index has been answered" in {

        val answers = emptyUserAnswers.set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("123", None)).success.value
        PreviousEuCountryPage(index).navigate(CheckMode, answers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(CheckMode, index, index))
      }
    }

    "must navigate in Amend mode" - {

      "to Previous EU VAT number for the same index where the VAT number hasn't already been answered" in {

        PreviousEuCountryPage(index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(AmendMode, index, index))
      }

      "to Add Previous Registration when the VAT number for this index has been answered" in {

        val answers = emptyUserAnswers.set(PreviousOssNumberPage(index, index), PreviousSchemeNumbers("123", None)).success.value
        PreviousEuCountryPage(index).navigate(AmendMode, answers)
          .mustEqual(prevRegRoutes.PreviousSchemeController.onPageLoad(AmendMode, index, index))
      }
    }

  }
}
