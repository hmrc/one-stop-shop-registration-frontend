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
import models.{CheckLoopMode, CheckMode, Index, InternationalAddress, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours
import pages.euDetails

class EuSendGoodsPageSpec extends SpecBase with PageBehaviours {

  private val index = Index(0)

  "EuSendGoodsPage" - {

    beRetrievable[Boolean](euDetails.EuSendGoodsPage(index))

    beSettable[Boolean](euDetails.EuSendGoodsPage(index))

    beRemovable[Boolean](euDetails.EuSendGoodsPage(index))

    "must navigate in Normal mode" - {
      "when the answer is yes" - {
        "to Eu Tax Reference when no Vat number provided" in {
          EuSendGoodsPage(index).navigate(
            NormalMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
          ) mustBe controllers.euDetails.routes.EuTaxReferenceController.onPageLoad(NormalMode, index)
        }

        "to Eu Send Goods Trading Name when Vat number provided" in {
          EuSendGoodsPage(index).navigate(
            NormalMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
              .set(EuVatNumberPage(index), "123").success.value
          ) mustBe controllers.euDetails.routes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, index)
        }
      }

      "when the answer is no" - {
        "to Check Eu Details Answers" in {
          EuSendGoodsPage(index).navigate(
            NormalMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), false).success.value
          ) mustBe controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
        }
      }

      "to Journey Recovery when no answer is provided" in {
        EuSendGoodsPage(index).navigate(NormalMode, emptyUserAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "must navigate in Check mode" - {
      "when the answer is yes" - {
        "to Eu Tax Reference when no Vat number provided and it hasn't been answered" in {
          EuSendGoodsPage(index).navigate(
            CheckMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
          ) mustBe controllers.euDetails.routes.EuTaxReferenceController.onPageLoad(CheckMode, index)
        }

        "to wherever Eu Tax Reference navigates when no Vat number provided and it has been answered" in {
          val answers = emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
            .set(EuTaxReferencePage(index), "123").success.value
          EuSendGoodsPage(index).navigate(
            CheckMode,
            answers
          ) mustBe EuTaxReferencePage(index).navigate(CheckMode, answers)
        }

        "to Eu Send Goods Trading Name when Vat number provided and it hasn't been answered" in {
          EuSendGoodsPage(index).navigate(
            CheckMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
              .set(EuVatNumberPage(index), "123").success.value
          ) mustBe controllers.euDetails.routes.EuSendGoodsTradingNameController.onPageLoad(CheckMode, index)
        }

        "to Eu Send Goods Trading Name when Vat number provided and it has been answered" in {
          val answers = emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
            .set(EuVatNumberPage(index), "123").success.value
            .set(EuSendGoodsTradingNamePage(index), "foo").success.value
          EuSendGoodsPage(index).navigate(
            CheckMode,
            answers
          ) mustBe EuSendGoodsTradingNamePage(index).navigate(CheckMode, answers)
        }
      }

      "when the answer is no" - {
        "to Check Eu Details Answers" in {
          EuSendGoodsPage(index).navigate(
            CheckMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), false).success.value
          ) mustBe controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, index)
        }
      }

      "to Journey Recovery when no answer is provided" in {
        EuSendGoodsPage(index).navigate(CheckMode, emptyUserAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "must navigate in CheckLoop mode" - {
      "when the answer is yes" - {
        "to Eu Tax Reference when no Vat number provided and it hasn't been answered" in {
          EuSendGoodsPage(index).navigate(
            CheckLoopMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
          ) mustBe controllers.euDetails.routes.EuTaxReferenceController.onPageLoad(CheckLoopMode, index)
        }

        "to wherever Eu Tax Reference navigates when no Vat number provided and it has been answered" in {
          val answers = emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
            .set(EuTaxReferencePage(index), "123").success.value
          EuSendGoodsPage(index).navigate(
            CheckLoopMode,
            answers
          ) mustBe EuTaxReferencePage(index).navigate(CheckLoopMode, answers)
        }

        "to Eu Send Goods Trading Name when Vat number provided and it hasn't been answered" in {
          EuSendGoodsPage(index).navigate(
            CheckLoopMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
              .set(EuVatNumberPage(index), "123").success.value
          ) mustBe controllers.euDetails.routes.EuSendGoodsTradingNameController.onPageLoad(CheckLoopMode, index)
        }

        "to Eu Send Goods Trading Name when Vat number provided and it has been answered" in {
          val answers = emptyUserAnswers.set(EuSendGoodsPage(index), true).success.value
            .set(EuVatNumberPage(index), "123").success.value
            .set(EuSendGoodsTradingNamePage(index), "foo").success.value
          EuSendGoodsPage(index).navigate(
            CheckLoopMode,
            answers
          ) mustBe EuSendGoodsTradingNamePage(index).navigate(CheckLoopMode, answers)
        }
      }

      "when the answer is no" - {
        "to CheckLoop Eu Details Answers" in {
          EuSendGoodsPage(index).navigate(
            CheckLoopMode,
            emptyUserAnswers.set(EuSendGoodsPage(index), false).success.value
          ) mustBe controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index)
        }
      }

      "to Journey Recovery when no answer is provided" in {
        EuSendGoodsPage(index).navigate(CheckLoopMode, emptyUserAnswers) mustBe controllers.routes.JourneyRecoveryController.onPageLoad()
      }
    }

    "must remove Send Goods Trading Name and Address for this index when the answer is no" in {
      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(HasFixedEstablishmentPage(Index(0)), false).success.value
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(EuSendGoodsTradingNamePage(Index(0)), "foo").success.value
          .set(EuSendGoodsAddressPage(Index(0)), address).success.value
      val result = answers.set(EuSendGoodsPage(Index(1)), false).success.value

      result.get(EuSendGoodsTradingNamePage(Index(1))) must not be defined
      result.get(EuSendGoodsAddressPage(Index(1))) must not be defined
    }

    "must remove EuTaxReference for this index when HasFixedEstablishment is no and answer is no" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(VatRegisteredPage(Index(0)), false).success.value
          .set(EuTaxReferencePage(Index(0)), "123456879").success.value
          .set(HasFixedEstablishmentPage(Index(0)), false).success.value
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(EuSendGoodsTradingNamePage(Index(0)), "foo").success.value
          .set(EuSendGoodsAddressPage(Index(0)), address).success.value

      val result = answers.set(EuSendGoodsPage(Index(0)), false).success.value
      result.get(EuTaxReferencePage(Index(0))) mustEqual None

    }

    "must preserve EuTaxReference for this index when HasFixedEstablishment is yes and answer is no" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(VatRegisteredPage(Index(0)), false).success.value
          .set(EuTaxReferencePage(Index(0)), "123456879").success.value
          .set(HasFixedEstablishmentPage(Index(0)), true).success.value
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(EuSendGoodsTradingNamePage(Index(0)), "foo").success.value
          .set(EuSendGoodsAddressPage(Index(0)), address).success.value

      val result = answers.set(EuSendGoodsPage(Index(0)), false).success.value
      result.get(EuTaxReferencePage(Index(0))) mustEqual Some("123456879")

    }

    "must preserve Send Goods Trading Name and Address when the answer is yes and remove Sends Goods answers" in {

      val address = arbitrary[InternationalAddress].sample.value

      val answers =
        UserAnswers("id")
          .set(HasFixedEstablishmentPage(Index(0)), false).success.value
          .set(EuSendGoodsPage(Index(0)), true).success.value
          .set(EuSendGoodsTradingNamePage(Index(0)), "foo").success.value
          .set(EuSendGoodsAddressPage(Index(0)), address).success.value

      val result = answers.set(EuSendGoodsPage(Index(0)), true).success.value

      result.get(EuSendGoodsTradingNamePage(Index(0))).value mustEqual "foo"
      result.get(EuSendGoodsAddressPage(Index(0))).value mustEqual address
      result.get(EuSendGoodsPage(Index(0))).value mustEqual true

    }
  }
}
