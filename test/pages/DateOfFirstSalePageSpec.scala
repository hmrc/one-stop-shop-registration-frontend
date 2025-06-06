/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.previousRegistrations.routes as prevRegRoutes
import controllers.routes
import models.{AmendMode, CheckMode, NormalMode, RejoinMode}
import org.scalacheck.Arbitrary
import pages.behaviours.PageBehaviours

import java.time.LocalDate

class DateOfFirstSalePageSpec extends SpecBase with PageBehaviours {

  "DateOfFirstSalePage" - {

    implicit lazy val arbitraryLocalDate: Arbitrary[LocalDate] = Arbitrary {
      datesBetween(LocalDate.of(1900, 1, 1), LocalDate.of(2100, 1, 1))
    }

    beRetrievable[LocalDate](DateOfFirstSalePage)

    beSettable[LocalDate](DateOfFirstSalePage)

    beRemovable[LocalDate](DateOfFirstSalePage)
  }

  "must navigate in Normal Mode" - {

    "to Previously Registered" in {

      DateOfFirstSalePage.navigate(NormalMode, emptyUserAnswers)
        .mustEqual(prevRegRoutes.PreviouslyRegisteredController.onPageLoad(NormalMode))
    }
  }

  "must navigate in Check Mode" - {

    "to Check Your Answers" in {

      DateOfFirstSalePage.navigate(CheckMode, emptyUserAnswers)
        .mustEqual(routes.CommencementDateController.onPageLoad(CheckMode))
    }
  }

  "must navigate in Amend Mode" - {

    "to Amend Your Answers" in {

      DateOfFirstSalePage.navigate(AmendMode, emptyUserAnswers)
        .mustEqual(routes.CommencementDateController.onPageLoad(AmendMode))
    }
  }

  "must navigate in Rejoin Mode" - {

    "to Rejoin Your Answers" in {

      DateOfFirstSalePage.navigate(RejoinMode, emptyUserAnswers)
        .mustEqual(routes.CommencementDateController.onPageLoad(RejoinMode))
    }
  }
}
