/*
 * Copyright 2022 HM Revenue & Customs
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
import models.{CheckLoopMode, CheckMode, Index, InternationalAddress, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class HasFixedEstablishmentPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "HasFixedEstablishmentPage" - {

    beRetrievable[Boolean](HasFixedEstablishmentPage(index))

    beSettable[Boolean](HasFixedEstablishmentPage(index))

    beRemovable[Boolean](HasFixedEstablishmentPage(index))

    "must navigate in Normal mode" - {

      "when the answer is yes" - {

          "to Fixed Establishment Trading Name for the same index" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
          }

      }

      "when the user answers no" - {

        "to Check EU Details Answers in Normal mode" in {

          val answers =
            emptyUserAnswers
              .set(HasFixedEstablishmentPage(index), false).success.value

          HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index))
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "and have not entered their Fixed Establishment Trading Name" - {

          "to Fixed Establishment Trading Name" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckMode, index))
          }
        }

        "and have already entered their Fixed Establishment Trading Name" - {

          "to wherever Fixed Establishment Trading Name would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value
                .set(FixedEstablishmentTradingNamePage(index), "foo").success.value

            HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
              .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers))
          }
        }
      }

      "when the user answers no" - {

        "to Check EU Details in Check mode" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
          HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
            .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when the answer is yes" - {

        "and have not entered their Fixed Establishment Trading Name" - {

          "to Fixed Establishment Trading Name" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value

            HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(CheckLoopMode, index))
          }
        }

        "and have already entered their Fixed Establishment Trading Name" - {

          "to wherever Fixed Establishment Trading Name would navigate to" in {

            val answers =
              emptyUserAnswers
                .set(VatRegisteredPage(index), true).success.value
                .set(HasFixedEstablishmentPage(index), true).success.value
                .set(FixedEstablishmentTradingNamePage(index), "foo").success.value

            HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
              .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers))
          }
        }
      }

      "when the user answers no" - {

        "to Check EU Details in Normal mode" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
          HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index))
        }
      }

      "when the user answers is empty" - {

        "to Journey recovery" in {

          HasFixedEstablishmentPage(index).navigate(CheckLoopMode, emptyUserAnswers)
            .mustEqual(controllers.routes.JourneyRecoveryController.onPageLoad())
        }
      }
    }

    "must remove Fixed Establishment Trading Name and Address for this index when the answer is no" in {

      val address1 = arbitrary[InternationalAddress].sample.value
      val address2 = arbitrary[InternationalAddress].sample.value

      println(address1)
      println(address2)

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address1).success.value
          .set(FixedEstablishmentTradingNamePage(Index(1)), "second").success.value
          .set(FixedEstablishmentAddressPage(Index(1)), address2).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(1)), false).success.value

      result.get(FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(FixedEstablishmentAddressPage(Index(0))).value mustEqual address1
      result.get(FixedEstablishmentTradingNamePage(Index(1))) must not be defined
      result.get(FixedEstablishmentAddressPage(Index(1))) must not be defined
    }

    "must preserve Fixed Establishment Trading Name and Address when the answer is no" in {

      val address = arbitrary[InternationalAddress].sample.value
      println(address)

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(0)), true).success.value

      result.get(FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(FixedEstablishmentAddressPage(Index(0))).value mustEqual address
    }
  }
}
