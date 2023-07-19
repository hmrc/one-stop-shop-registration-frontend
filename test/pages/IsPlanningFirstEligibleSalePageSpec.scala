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
import controllers.previousRegistrations.{routes => prevRegRoutes}
import controllers.amend.{routes => amendRoutes}
import models.{AmendMode, CheckMode, NormalMode}
import pages.behaviours.PageBehaviours

class IsPlanningFirstEligibleSalePageSpec extends SpecBase with PageBehaviours {

  "IsPlanningFirstEligibleSalePage" - {

    beRetrievable[Boolean](IsPlanningFirstEligibleSalePage)

    beSettable[Boolean](IsPlanningFirstEligibleSalePage)

    beRemovable[Boolean](IsPlanningFirstEligibleSalePage)

    "must navigate in Normal mode" - {

      "to Previously Registered page when true is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          NormalMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, true).success.value
        ).mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
      }

      "to Register Later page when false is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          NormalMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, false).success.value
        ).mustEqual(routes.RegisterLaterController.onPageLoad())
      }

      "to Journey recovery when answer is empty" in {

        IsPlanningFirstEligibleSalePage.navigate(
          NormalMode, emptyUserAnswers).mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "to Previously Registered page when true is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          CheckMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, true).success.value
        ).mustEqual(routes.CommencementDateController.onPageLoad(CheckMode))
      }

      "to Register Later page when false is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          CheckMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, false).success.value
        ).mustEqual(routes.RegisterLaterController.onPageLoad())
      }

      "to Journey recovery when answer is empty" in {

        IsPlanningFirstEligibleSalePage.navigate(
          CheckMode, emptyUserAnswers).mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "to Previously Registered page when true is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          AmendMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, true).success.value
        ).mustEqual(routes.CommencementDateController.onPageLoad(AmendMode))
      }

      "to Register Later page when false is submitted" in {

        IsPlanningFirstEligibleSalePage.navigate(
          AmendMode, emptyUserAnswers.set(IsPlanningFirstEligibleSalePage, false).success.value
        ).mustEqual(routes.RegisterLaterController.onPageLoad())
      }

      "to Amend Journey recovery when answer is empty" in {

        IsPlanningFirstEligibleSalePage.navigate(
          AmendMode, emptyUserAnswers).mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

  }
}
