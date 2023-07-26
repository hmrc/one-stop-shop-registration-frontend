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

package pages

import base.SpecBase
import controllers.routes
import controllers.amend.{routes => amendRoutes}
import models.{AmendMode, CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class HasMadeSalesPageSpec extends SpecBase with PageBehaviours {

  "HasMadeSalesPage" - {

    beRetrievable[Boolean](HasMadeSalesPage)

    beSettable[Boolean](HasMadeSalesPage)

    beRemovable[Boolean](HasMadeSalesPage)

    "must navigate in NormalMode" - {
      "must navigate to Date Of First Sale page when the answer is yes" in {
        HasMadeSalesPage.navigate(
          NormalMode,
          emptyUserAnswers.set(
            HasMadeSalesPage,
            true
          ).success.value) mustEqual controllers.routes.DateOfFirstSaleController.onPageLoad(NormalMode)
      }

      "must navigate to Is Planning First Eligible Sale page when the answer is no" in {
        HasMadeSalesPage.navigate(NormalMode,
          emptyUserAnswers.set(
            HasMadeSalesPage,
            false
          ).success.value) mustEqual controllers.routes.IsPlanningFirstEligibleSaleController.onPageLoad(NormalMode)
      }

      "to JourneyRecoveryController and answer is empty" in {
        HasMadeSalesPage.navigate(NormalMode, basicUserAnswersWithVatInfo)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "to DateOfFirstSalePage and answer is Some(true)" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(HasMadeSalesPage, true).success.value

        HasMadeSalesPage.navigate(CheckMode, userAnswers)
          .mustEqual(routes.DateOfFirstSaleController.onPageLoad(CheckMode))
      }

      "to IsPlanningFirstEligibleSaleController and answer is Some(true)" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(HasMadeSalesPage, false).success.value

        HasMadeSalesPage.navigate(CheckMode, userAnswers)
          .mustEqual(routes.IsPlanningFirstEligibleSaleController.onPageLoad(CheckMode))
      }

      "to JourneyRecoveryController and answer is empty" in {
        HasMadeSalesPage.navigate(CheckMode, basicUserAnswersWithVatInfo)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "to DateOfFirstSalePage and answer is Some(true)" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(HasMadeSalesPage, true).success.value

        HasMadeSalesPage.navigate(AmendMode, userAnswers)
          .mustEqual(routes.DateOfFirstSaleController.onPageLoad(AmendMode))
      }

      "to IsPlanningFirstEligibleSaleController and answer is Some(true)" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(HasMadeSalesPage, false).success.value

        HasMadeSalesPage.navigate(AmendMode, userAnswers)
          .mustEqual(routes.IsPlanningFirstEligibleSaleController.onPageLoad(AmendMode))
      }

      "to AmendJourneyRecoveryController and answer is empty" in {
        HasMadeSalesPage.navigate(AmendMode, basicUserAnswersWithVatInfo)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }


    "cleanup" - {

      "must remove IsPlanningFirstEligibleSalePage when HasMadeSales is true" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(IsPlanningFirstEligibleSalePage, true).success.value
        val result = HasMadeSalesPage.cleanup(Some(true), userAnswers).success.value

        result mustBe basicUserAnswersWithVatInfo
      }

      "must remove DateOfFirstSalePage when HasMadeSales is false" in {
        val userAnswers = basicUserAnswersWithVatInfo
          .set(DateOfFirstSalePage, LocalDate.now()).success.value
        val result = HasMadeSalesPage.cleanup(Some(false), userAnswers).success.value

        result mustBe basicUserAnswersWithVatInfo
      }

      "must return user answers when HasMadeSales is None" in {
        val result = HasMadeSalesPage.cleanup(None, basicUserAnswersWithVatInfo).success.value

        result mustBe basicUserAnswersWithVatInfo
      }
    }
  }
}
