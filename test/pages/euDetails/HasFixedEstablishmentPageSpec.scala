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

        "to Eu Tax Reference when no Vat Number provided" in {

          val answers =
            emptyUserAnswers
              .set(VatRegisteredPage(index), false).success.value
              .set(HasFixedEstablishmentPage(index), true).success.value

          HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuTaxReferenceController.onPageLoad(NormalMode, index))
        }

        "to Fixed Establishment Trading Name for the same index when Vat number is provided" in {

          val answers =
            emptyUserAnswers
              .set(VatRegisteredPage(index), true).success.value
              .set(EuVatNumberPage(index), "123").success.value
              .set(HasFixedEstablishmentPage(index), true).success.value

          HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.FixedEstablishmentTradingNameController.onPageLoad(NormalMode, index))
        }

      }

      "when the user answers no" - {

        "to Eu Send Goods in Normal mode" in {

          val answers =
            emptyUserAnswers
              .set(HasFixedEstablishmentPage(index), false).success.value

          HasFixedEstablishmentPage(index).navigate(NormalMode, answers)
            .mustEqual(euRoutes.EuSendGoodsController.onPageLoad(NormalMode, index))
        }
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "and the user provided a Vat number" - {

          "and have not entered their Fixed Establishment Trading Name" - {

            "to Fixed Establishment Trading Name" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), true).success.value
                  .set(EuVatNumberPage(index), "12345").success.value
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
                  .set(EuVatNumberPage(index), "12345").success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value
                  .set(FixedEstablishmentTradingNamePage(index), "foo").success.value

              HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
                .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckMode, answers))
            }
          }
        }

        "and the user have not provided a Vat number" - {

          "and have not entered their Eu Tax Reference" - {

            "to Eu Tax Reference" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), false).success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value

              HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
                .mustEqual(euRoutes.EuTaxReferenceController.onPageLoad(CheckMode, index))
            }
          }

          "and have already entered their Eu Tax Reference" - {

            "to wherever Eu Tax Reference navigates" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), false).success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value
                  .set(EuTaxReferencePage(index), "12345").success.value

              HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
                .mustEqual(EuTaxReferencePage(index).navigate(CheckMode, answers))
            }
          }
        }
      }

      "when the user answers no" - {

        "to Eu Send Goods in Check mode if it hasn't been answered" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
          HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
            .mustEqual(euRoutes.EuSendGoodsController.onPageLoad(CheckMode, index))
        }

        "to wherever Eu Send Goods navigates in Check mode if it has been answered" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
            .set(EuSendGoodsPage(index), true).success.value
          HasFixedEstablishmentPage(index).navigate(CheckMode, answers)
            .mustEqual(EuSendGoodsPage(index).navigate(CheckMode, answers))
        }
      }
    }

    "must navigate in Check Loop mode" - {

      "when the answer is yes" - {

        "and the user provided a Vat number" - {

          "and have not entered their Fixed Establishment Trading Name" - {

            "to Fixed Establishment Trading Name" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), true).success.value
                  .set(EuVatNumberPage(index), "12345").success.value
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
                  .set(EuVatNumberPage(index), "12345").success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value
                  .set(FixedEstablishmentTradingNamePage(index), "foo").success.value

              HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
                .mustEqual(FixedEstablishmentTradingNamePage(index).navigate(CheckLoopMode, answers))
            }
          }
        }

        "and the user have not provided a Vat number" - {

          "and have not entered their Eu Tax Reference" - {

            "to Eu Tax Reference" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), false).success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value

              HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
                .mustEqual(euRoutes.EuTaxReferenceController.onPageLoad(CheckLoopMode, index))
            }
          }

          "and have already entered their Eu Tax Reference" - {

            "to wherever Eu Tax Reference navigates" in {

              val answers =
                emptyUserAnswers
                  .set(VatRegisteredPage(index), false).success.value
                  .set(HasFixedEstablishmentPage(index), true).success.value
                  .set(EuTaxReferencePage(index), "12345").success.value

              HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
                .mustEqual(EuTaxReferencePage(index).navigate(CheckLoopMode, answers))
            }
          }
        }
      }

      "when the user answers no" - {

        "to Eu Send Goods in Check Loop mode if it hasn't been answered" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
          HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
            .mustEqual(euRoutes.EuSendGoodsController.onPageLoad(CheckLoopMode, index))
        }

        "to wherever Eu Send Goods navigates in Check Loop mode if it has been answered" in {

          val answers = emptyUserAnswers.set(HasFixedEstablishmentPage(index), false).success.value
            .set(EuSendGoodsPage(index), true).success.value
          HasFixedEstablishmentPage(index).navigate(CheckLoopMode, answers)
            .mustEqual(EuSendGoodsPage(index).navigate(CheckLoopMode, answers))
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

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
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

    "must remove EuTaxReference for this index when EuSendGoodsPage is no and answer is no" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(EuSendGoodsPage(Index(0)), false).success.value
          .set(VatRegisteredPage(Index(0)), false).success.value
          .set(EuTaxReferencePage(Index(0)), "123456879").success.value
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(0)), false).success.value
      result.get(EuTaxReferencePage(Index(0))) mustEqual None

    }

    "must preserve EuTaxReference for this index when EuSendGoodsPage is yes and answer is no" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(VatRegisteredPage(Index(0)), false).success.value
          .set(EuTaxReferencePage(Index(0)), "123456879").success.value
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(0)), false).success.value
      result.get(EuTaxReferencePage(Index(0))) mustEqual Some("123456879")

    }

    "must preserve Fixed Establishment Trading Name and Address when the answer is yes and remove Sends Goods answers" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(FixedEstablishmentTradingNamePage(Index(0)), "first").success.value
          .set(FixedEstablishmentAddressPage(Index(0)), address).success.value
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(EuSendGoodsTradingNamePage(Index(0)), "foo").success.value
          .set(EuSendGoodsAddressPage(Index(0)), arbitraryInternationalAddress.arbitrary.sample.value).success.value

      val result = answers.set(HasFixedEstablishmentPage(Index(0)), true).success.value

      result.get(FixedEstablishmentTradingNamePage(Index(0))).value mustEqual "first"
      result.get(FixedEstablishmentAddressPage(Index(0))).value mustEqual address
      result.get(EuSendGoodsPage(Index(1))) must not be defined
      result.get(EuSendGoodsTradingNamePage(Index(1))) must not be defined
      result.get(EuSendGoodsAddressPage(Index(1))) must not be defined

    }
  }
}
