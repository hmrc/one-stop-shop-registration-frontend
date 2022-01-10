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

package pages

import base.SpecBase
import controllers.routes
import models.{CheckMode, InternationalAddress, NormalMode, UkAddress}
import org.scalacheck.Arbitrary.arbitrary
import pages.behaviours.PageBehaviours

class BusinessAddressInUkPageSpec extends SpecBase with PageBehaviours {

  private val internationalAddress = arbitrary[InternationalAddress].sample.value
  private val ukAddress            = arbitrary[UkAddress].sample.value

  "BusinessAddressInUkPage" - {

    beRetrievable[Boolean](BusinessAddressInUkPage)

    beSettable[Boolean](BusinessAddressInUkPage)

    beRemovable[Boolean](BusinessAddressInUkPage)

    "must navigate in Normal mode" - {

      "to UK address when the answer is yes" in {

        val answers = emptyUserAnswers.set(BusinessAddressInUkPage, true).success.value
        BusinessAddressInUkPage.navigate(NormalMode, answers)
          .mustEqual(routes.UkAddressController.onPageLoad(NormalMode))
      }

      "to International address when the answer is no" in {

        val answers = emptyUserAnswers.set(BusinessAddressInUkPage, false).success.value
        BusinessAddressInUkPage.navigate(NormalMode, answers)
          .mustEqual(routes.InternationalAddressController.onPageLoad(NormalMode))
      }

      "to Journey recovery address when the answer is none" in {
        BusinessAddressInUkPage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check Mode" - {

      "when the answer is yes" - {

        "to UK address if UK Address has not already been answered" in {

          val answers = emptyUserAnswers.set(BusinessAddressInUkPage, true).success.value
          BusinessAddressInUkPage.navigate(CheckMode, answers)
            .mustEqual(routes.UkAddressController.onPageLoad(CheckMode))
        }

        "to Check Your Answers if UK Address has already been answered" in {

          val answers =
            emptyUserAnswers
              .set(BusinessAddressInUkPage, true).success.value
              .set(UkAddressPage, ukAddress).success.value

          BusinessAddressInUkPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "when the answer is no" - {

        "to International address if International Address has not already been answered" in {

          val answers = emptyUserAnswers.set(BusinessAddressInUkPage, false).success.value
          BusinessAddressInUkPage.navigate(CheckMode, answers)
            .mustEqual(routes.InternationalAddressController.onPageLoad(CheckMode))
        }

        "to Check Your Answers if International Address has already been answered" in {

          val answers =
            emptyUserAnswers
              .set(BusinessAddressInUkPage, false).success.value
              .set(InternationalAddressPage, internationalAddress).success.value

          BusinessAddressInUkPage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "to Journey recovery address when the answer is none" in {
        BusinessAddressInUkPage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must remove International Address when the answer is yes" in {

      val answers =
        emptyUserAnswers
          .set(UkAddressPage, ukAddress).success.value
          .set(InternationalAddressPage, internationalAddress).success.value

      val result = answers.set(BusinessAddressInUkPage, true).success.value

      result.get(UkAddressPage).value mustEqual ukAddress
      result.get(InternationalAddressPage) must not be defined
    }

    "must remove UK address and when the answer is no" in {

      val answers =
        emptyUserAnswers
          .set(UkAddressPage, ukAddress).success.value
          .set(InternationalAddressPage, internationalAddress).success.value

      val result = answers.set(BusinessAddressInUkPage, false).success.value

      result.get(UkAddressPage) must not be defined
      result.get(InternationalAddressPage).value mustEqual internationalAddress
    }
  }
}
