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
import models.{CheckLoopMode, CheckMode, Country, Index, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import pages.behaviours.PageBehaviours
import pages.euDetails


class EuCountryPageSpec extends SpecBase with PageBehaviours with ScalaCheckPropertyChecks {

  private val countryIndex = Index(0)

  "EuCountryPage" - {

    beRetrievable[Country](EuCountryPage(countryIndex))

    beSettable[Country](euDetails.EuCountryPage(countryIndex))

    beRemovable[Country](euDetails.EuCountryPage(countryIndex))

    "must navigate in Normal mode" - {

      "to Sells Goods to EU Consumer for the same index" in {

        EuCountryPage(countryIndex).navigate(NormalMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
          .mustEqual(euRoutes.SellsGoodsToEUConsumersController.onPageLoad(NormalMode, countryIndex))
      }
    }

    "must navigate in Check mode" - {

      "when Eu Vat Number has not been answered" - {

        "to Eu Vat Number for the same index" in {

          EuCountryPage(countryIndex).navigate(CheckMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
            .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckMode, countryIndex))
        }
      }

      "when Eu Vat Number has been answered" - {

        "to wherever Eu Vat Number would navigate to in Check mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
                .set(EuVatNumberPage(Index(0)), "1234567").success.value

              EuCountryPage(countryIndex).navigate(CheckMode, answers)
                .mustEqual(EuVatNumberPage(Index(0)).navigate(CheckMode, answers))
          }
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when Eu Vat Number has not been answered" - {

        "to Eu Vat Number for the same index" in {

          EuCountryPage(countryIndex).navigate(CheckLoopMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
            .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, countryIndex))
        }
      }

      "when Eu Vat Number has been answered" - {

        "to wherever Eu Vat Number would navigate to in Check Loop mode" in {

          forAll(arbitrary[UserAnswers]) {
            baseAnswers =>

              val answers = baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
                .set(EuVatNumberPage(Index(0)), "1234567").success.value

              EuCountryPage(countryIndex).navigate(CheckLoopMode, answers)
                .mustEqual(EuVatNumberPage(Index(0)).navigate(CheckLoopMode, answers))
          }
        }
      }
    }
  }
}
