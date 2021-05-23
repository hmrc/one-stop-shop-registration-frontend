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

import models.{Country, Index, UserAnswers}
import pages.behaviours.PageBehaviours

class VatRegisteredInEuPageSpec extends PageBehaviours {

  "VatRegisteredInEuPage" - {

    beRetrievable[Boolean](VatRegisteredInEuPage)

    beSettable[Boolean](VatRegisteredInEuPage)

    beRemovable[Boolean](VatRegisteredInEuPage)

    "must remove all EU VAT details when the answer is false" in {

      val answers =
        UserAnswers("id")
          .set(VatRegisteredEuMemberStatePage(Index(0)), Country.euCountries.head).success.value
          .set(EuVatNumberPage(Index(0)), "reg 1").success.value
          .set(VatRegisteredEuMemberStatePage(Index(1)), Country.euCountries.tail.head).success.value
          .set(EuVatNumberPage(Index(1)), "reg 2").success.value

      val result = answers.set(VatRegisteredInEuPage, false).success.value

      result.get(VatRegisteredEuMemberStatePage(Index(0))) must not be defined
      result.get(EuVatNumberPage(Index(0))) must not be defined
      result.get(VatRegisteredEuMemberStatePage(Index(1))) must not be defined
      result.get(EuVatNumberPage(Index(1))) must not be defined
    }

    "must not remove any EU VAT details when the answer is true" in {

      val answers =
        UserAnswers("id")
          .set(VatRegisteredEuMemberStatePage(Index(0)), Country.euCountries.head).success.value
          .set(EuVatNumberPage(Index(0)), "reg 1").success.value
          .set(VatRegisteredEuMemberStatePage(Index(1)), Country.euCountries.tail.head).success.value
          .set(EuVatNumberPage(Index(1)), "reg 2").success.value

      val result = answers.set(VatRegisteredInEuPage, true).success.value

      result.get(VatRegisteredEuMemberStatePage(Index(0))).value mustEqual Country.euCountries.head
      result.get(EuVatNumberPage(Index(0))).value mustEqual "reg 1"
      result.get(VatRegisteredEuMemberStatePage(Index(1))).value mustEqual Country.euCountries.tail.head
      result.get(EuVatNumberPage(Index(1))).value mustEqual "reg 2"
    }
  }
}
