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
import controllers.amend.routes as amendRoutes
import controllers.rejoin.routes as rejoinRoutes
import controllers.routes
import models.{AmendMode, CheckMode, NormalMode, RejoinMode}
import pages.behaviours.PageBehaviours

class IsOnlineMarketplacePageSpec extends SpecBase with PageBehaviours {

  "IsOnlineMarketplacePage" - {

    beRetrievable[Boolean](IsOnlineMarketplacePage)

    beSettable[Boolean](IsOnlineMarketplacePage)

    beRemovable[Boolean](IsOnlineMarketplacePage)

    "must navigate in Normal mode" - {

      "to Has Website" in {

        IsOnlineMarketplacePage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.HasWebsiteController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Check Your Answers" in {

        IsOnlineMarketplacePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.CheckYourAnswersController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "to Change Your Registration" in {

        IsOnlineMarketplacePage.navigate(AmendMode, emptyUserAnswers)
          .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
      }
    }

    "must navigate in Rejoin mode" - {

      "to Rejoin Registration" in {

        IsOnlineMarketplacePage.navigate(RejoinMode, emptyUserAnswers)
          .mustEqual(rejoinRoutes.RejoinRegistrationController.onPageLoad())
      }
    }
  }
}
