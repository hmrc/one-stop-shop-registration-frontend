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
import models.euDetails.FixedEstablishmentAddress
import models.{Index, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.euDetails

class HasFixedEstablishmentPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "HasFixedEstablishmentPage" - {

    beRetrievable[Boolean](HasFixedEstablishmentPage(index))

    beSettable[Boolean](euDetails.HasFixedEstablishmentPage(index))

    beRemovable[Boolean](euDetails.HasFixedEstablishmentPage(index))

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

        "and the user answered that they are VAT registered in this country" - {

          "to Fixed Establishment Trading Name for the same index" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
          }
        }

        "and the user answered that they are not VAT registered in this country" - {

          "to EU Tax Reference for the same index" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), false).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
              .mustEqual(euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index))
          }
        }
      }

      "when the user answers no" - {

        "to Check EU Details Answers" in {

          val answers =
            emptyUserAnswers
              .set(HasFixedEstablishmentPage(index), false).success.value

          HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(index))
        }
      }
    }

    "must remove Fixed Establishment Trading Name and Address for this index when the answer is no" in {

      val address1 = arbitrary[FixedEstablishmentAddress].sample.value
      val address2 = arbitrary[FixedEstablishmentAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address1).success.value
          .set(euDetails.FixedEstablishmentTradingNamePage(Index(1)), "second").success.value
          .set(euDetails.FixedEstablishmentAddressPage(Index(1)), address2).success.value

      val result = answers.set(euDetails.HasFixedEstablishmentPage(Index(1)), false).success.value

      result.get(euDetails.FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(euDetails.FixedEstablishmentAddressPage(Index(0))).value mustEqual address1
      result.get(euDetails.FixedEstablishmentTradingNamePage(Index(1))) mustBe empty
      result.get(euDetails.FixedEstablishmentAddressPage(Index(1))) mustBe empty
    }

    "must preserve Fixed Establishment Trading Name and Address when the answer is no" in {

      val address = arbitrary[FixedEstablishmentAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(euDetails.FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(euDetails.FixedEstablishmentAddressPage(Index(0)), address).success.value

      val result = answers.set(euDetails.HasFixedEstablishmentPage(Index(0)), true).success.value

      result.get(euDetails.FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(euDetails.FixedEstablishmentAddressPage(Index(0))).value mustEqual address
    }
  }
}
