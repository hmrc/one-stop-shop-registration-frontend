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

  private val index = Index(0)

  "EuCountryPage" - {

    beRetrievable[Country](EuCountryPage(index))

    beSettable[Country](euDetails.EuCountryPage(index))

    beRemovable[Country](euDetails.EuCountryPage(index))

    "must navigate in Normal mode" - {

      "to Vat Registered for the same index when user is not part of vat group" in {

        EuCountryPage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(euRoutes.VatRegisteredController.onPageLoad(NormalMode, index))
      }

      "to Eu Vat Number for the same index when user is part of vat group" in {

        EuCountryPage(index).navigate(NormalMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
          .mustEqual(euRoutes.EuVatNumberController.onPageLoad(NormalMode, index))
      }
    }

    "must navigate in Check mode" - {

      "when user is not part of vat group" - {
        "when Vat Registered has not been answered" - {

          "to Vat Registered for the same index" in {

            EuCountryPage(index).navigate(CheckMode, emptyUserAnswers)
              .mustEqual(euRoutes.VatRegisteredController.onPageLoad(CheckMode, index))
          }
        }

        "when Vat Registered has been answered" - {

          "to wherever Vat Registered would navigate to in Check mode" in {

            forAll(arbitrary[UserAnswers]) {
              baseAnswers =>

                val answers = baseAnswers
                  .set(VatRegisteredPage(Index(0)), true).success.value

                EuCountryPage(index).navigate(CheckMode, answers)
                  .mustEqual(VatRegisteredPage(Index(0)).navigate(CheckMode, answers))
            }
          }
        }
      }

      "when user is part of vat group" - {
        "when Eu Vat Number has not been answered" - {

          "to Eu Vat Number for the same index" in {

            EuCountryPage(index).navigate(CheckMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckMode, index))
          }
        }

        "when Eu Vat Number has been answered" - {

          "to wherever Eu Vat Number would navigate to in Check mode" in {

            forAll(arbitrary[UserAnswers]) {
              baseAnswers =>

                val answers = baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
                  .set(EuVatNumberPage(Index(0)), "1234567").success.value

                EuCountryPage(index).navigate(CheckMode, answers)
                  .mustEqual(EuVatNumberPage(Index(0)).navigate(CheckMode, answers))
            }
          }
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when the user is not a part of vat group" - {
        "when Vat Registered has not been answered" - {

          "to Vat Registered for the same index" in {

            EuCountryPage(index).navigate(CheckLoopMode, emptyUserAnswers)
              .mustEqual(euRoutes.VatRegisteredController.onPageLoad(CheckLoopMode, index))
          }
        }

        "when Vat Registered has been answered" - {

          "to wherever Vat Registered would navigate to in Check Loop mode" in {

            forAll(arbitrary[UserAnswers]) {
              baseAnswers =>

                val answers = baseAnswers
                  .set(VatRegisteredPage(Index(0)), true).success.value

                EuCountryPage(index).navigate(CheckLoopMode, answers)
                  .mustEqual(VatRegisteredPage(Index(0)).navigate(CheckLoopMode, answers))
            }
          }
        }
      }

      "when the user is a part of vat group" - {
        "when Eu Vat Number has not been answered" - {

          "to Eu Vat Number for the same index" in {

            EuCountryPage(index).navigate(CheckLoopMode, emptyUserAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true))))
              .mustEqual(euRoutes.EuVatNumberController.onPageLoad(CheckLoopMode, index))
          }
        }

        "when Eu Vat Number has been answered" - {

          "to wherever Eu Vat Number would navigate to in Check Loop mode" in {

            forAll(arbitrary[UserAnswers]) {
              baseAnswers =>

                val answers = baseAnswers.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
                  .set(EuVatNumberPage(Index(0)), "1234567").success.value

                EuCountryPage(index).navigate(CheckLoopMode, answers)
                  .mustEqual(EuVatNumberPage(Index(0)).navigate(CheckLoopMode, answers))
            }
          }
        }
      }
    }
  }
}
