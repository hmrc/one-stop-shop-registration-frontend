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
import models.{AmendMode, CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class AddWebsitePageSpec extends SpecBase with PageBehaviours {

  "AddWebsitePage" - {

    beRetrievable[Boolean](AddWebsitePage)

    beSettable[Boolean](AddWebsitePage)

    beRemovable[Boolean](AddWebsitePage)

    "must navigate in Normal mode" - {

      "to Website with an index equal to the number of existing websites when the answer is yes" in {

        val answers =
          emptyUserAnswers
            .set(WebsitePage(Index(0)), "foo").success.value
            .set(WebsitePage(Index(1)), "bar").success.value
            .set(AddWebsitePage, true).success.value

        AddWebsitePage.navigate(NormalMode, answers)
          .mustEqual(routes.WebsiteController.onPageLoad(NormalMode, Index(2)))
      }

      "to Contact Details when the answer is no" in {

        val answers = emptyUserAnswers.set(AddWebsitePage, false).success.value

        AddWebsitePage.navigate(NormalMode, answers)
          .mustEqual(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
      }

      "to Journey recovery when the answer is none" in {
        AddWebsitePage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Website with index equal to the number of websites already answered" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "foo").success.value
              .set(WebsitePage(Index(1)), "bar").success.value
              .set(AddWebsitePage, true).success.value

          AddWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.WebsiteController.onPageLoad(CheckMode, Index(2)))
        }
      }

      "when the answer is no" - {

        "to Check Your Answers" in {

          val answers = emptyUserAnswers.set(AddWebsitePage, false).success.value

          AddWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "to Journey recovery when the answer is none" in {
        AddWebsitePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "when the answer is yes" - {

        "to Website with index equal to the number of websites already answered" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "foo").success.value
              .set(WebsitePage(Index(1)), "bar").success.value
              .set(AddWebsitePage, true).success.value

          AddWebsitePage.navigate(AmendMode, answers)
            .mustEqual(routes.WebsiteController.onPageLoad(AmendMode, Index(2)))
        }
      }

      "when the answer is no" - {

        "to Change Your Registration" in {

          val answers = emptyUserAnswers.set(AddWebsitePage, false).success.value

          AddWebsitePage.navigate(AmendMode, answers)
            .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
        }
      }

      "to Amend Journey recovery when the answer is none" in {
        AddWebsitePage.navigate(AmendMode, emptyUserAnswers)
          .mustEqual(routes.AmendJourneyRecoveryController.onPageLoad())
      }
    }

  }
}
