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
import controllers.euDetails.routes
import models.{AmendMode, Index, NormalMode}
import models.euDetails.RegistrationType
import pages.behaviours.PageBehaviours

class RegistrationTypePageSpec extends SpecBase with PageBehaviours {

  private val countryIndex: Index = Index(0)

  "RegistrationTypePage" - {

    beRetrievable[RegistrationType](RegistrationTypePage(countryIndex))

    beSettable[RegistrationType](RegistrationTypePage(countryIndex))

    beRemovable[RegistrationType](RegistrationTypePage(countryIndex))

    "must navigate" - {

      "to EU VAT number when the answer is Vat number" in {

        val answers = emptyUserAnswers
          .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value

        RegistrationTypePage(countryIndex).navigate(NormalMode, answers) mustEqual routes.EuVatNumberController.onPageLoad(NormalMode, countryIndex)
      }

      "to EU Tax Reference when the answer Tax Id" in {

        val answers = emptyUserAnswers
          .set(RegistrationTypePage(countryIndex), RegistrationType.TaxId).success.value

        RegistrationTypePage(countryIndex).navigate(NormalMode, answers) mustEqual routes.EuTaxReferenceController.onPageLoad(NormalMode, countryIndex)
      }
    }

    "must navigate in Amend Mode" - {

      "to EU VAT number when the answer is Vat number" in {

        val answers = emptyUserAnswers
          .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value

        RegistrationTypePage(countryIndex).navigate(AmendMode, answers) mustEqual routes.EuVatNumberController.onPageLoad(AmendMode, countryIndex)
      }

      "to EU Tax Reference when the answer Tax Id" in {

        val answers = emptyUserAnswers
          .set(RegistrationTypePage(countryIndex), RegistrationType.TaxId).success.value

        RegistrationTypePage(countryIndex).navigate(AmendMode, answers) mustEqual routes.EuTaxReferenceController.onPageLoad(AmendMode, countryIndex)
      }
    }
  }
}

