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
import controllers.routes
import controllers.amend.{routes => amendRoutes}
import models.{AmendMode, CheckMode, Index, NormalMode}
import pages.behaviours.PageBehaviours

class HasWebsitePageSpec extends SpecBase with PageBehaviours {

  "HasWebsitePage" - {

    beRetrievable[Boolean](HasWebsitePage)

    beSettable[Boolean](HasWebsitePage)

    beRemovable[Boolean](HasWebsitePage)

    "must navigate in Normal mode" - {

      "to Website (index 0) when the answer is yes" in {

        val answers = emptyUserAnswers.set(HasWebsitePage, true).success.value
        HasWebsitePage.navigate(NormalMode, answers)
          .mustEqual(routes.WebsiteController.onPageLoad(NormalMode, Index(0)))
      }

      "to Contact Details when the answer is no" in {

        val answers = emptyUserAnswers.set(HasWebsitePage, false).success.value
        HasWebsitePage.navigate(NormalMode, answers)
          .mustEqual(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
      }

      "to Journey recovery when the answer is empty" in {

        HasWebsitePage.navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Website (index 0) when there are no websites in the user's answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage ,true).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.WebsiteController.onPageLoad(CheckMode, Index(0)))
        }

        "to Add Websites when there are websites in the user's answers" in {

          val answers = emptyUserAnswers
            .set(WebsitePage(Index(0)), "foo").success.value
            .set(HasWebsitePage, true).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.AddWebsiteController.onPageLoad(CheckMode))
        }
      }

      "when the answer is no" - {

        "to Delete All Websites when there are websites in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasWebsitePage, false).success.value
            .set(WebsitePage(Index(0)), "foo").success.value
            .set(WebsitePage(Index(1)), "bar").success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.DeleteAllWebsitesController.onPageLoad(CheckMode))
        }

        "to Check Your Answers when there are no websites in the user's answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, false).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "to Journey recovery when the answer is empty" in {

        HasWebsitePage.navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.JourneyRecoveryController.onPageLoad())
      }
    }

    "must navigate in Amend mode" - {

      "when the answer is yes" - {

        "to Website (index 0) when there are no websites in the user's answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, true).success.value

          HasWebsitePage.navigate(AmendMode, answers)
            .mustEqual(routes.WebsiteController.onPageLoad(AmendMode, Index(0)))
        }

        "to Add Website when there are websites in the user's answers" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "foo").success.value
              .set(HasWebsitePage, true).success.value

          HasWebsitePage.navigate(AmendMode, answers)
            .mustEqual(routes.AddWebsiteController.onPageLoad(AmendMode))
        }
      }

      "when the answer is no" - {

        "to Delete All Websites when there are websites in the user's answers" in {

          val answers = emptyUserAnswers
            .set(HasWebsitePage, false).success.value
            .set(WebsitePage(Index(0)), "foo").success.value
            .set(WebsitePage(Index(1)), "bar").success.value

          HasWebsitePage.navigate(AmendMode, answers)
            .mustEqual(routes.DeleteAllWebsitesController.onPageLoad(AmendMode))
        }

        "to Change Your Registration when there are no websites in the user's answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, false).success.value

          HasWebsitePage.navigate(AmendMode, answers)
            .mustEqual(amendRoutes.ChangeYourRegistrationController.onPageLoad())
        }
      }

      "to Amend Journey recovery when the answer is empty" in {

        HasWebsitePage.navigate(AmendMode, emptyUserAnswers)
          .mustEqual(amendRoutes.AmendJourneyRecoveryController.onPageLoad())
      }
    }
  }
}