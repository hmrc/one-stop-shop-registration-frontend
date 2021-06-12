/*
 * Copyright 2021 HM Revenue & Customs
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
import models.{CheckMode, Index, NormalMode, UserAnswers}
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
    }

    "must navigate in Check mode" - {

      "when the answer is yes" - {

        "to Website (index 0) when there are no websites in the user's answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage ,true).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.WebsiteController.onPageLoad(CheckMode, Index(0)))
        }

        "to Check Your Answers when there are websites in the user's answers" in {

          val answers =
            emptyUserAnswers
              .set(WebsitePage(Index(0)), "foo").success.value
              .set(HasWebsitePage ,true).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }

      "when the answer is no" - {

        "to Check Your Answers" in {

          val answers = emptyUserAnswers.set(HasWebsitePage, false).success.value

          HasWebsitePage.navigate(CheckMode, answers)
            .mustEqual(routes.CheckYourAnswersController.onPageLoad())
        }
      }
    }

    "must remove all websites when the answer is no" in {

      val answers =
        UserAnswers("id")
          .set(WebsitePage(Index(0)), "website 1").success.value
          .set(WebsitePage(Index(1)), "website 2").success.value

      val result = answers.set(HasWebsitePage, false).success.value

      result.get(WebsitePage(Index(0))) must not be defined
      result.get(WebsitePage(Index(1))) must not be defined
    }

    "must not remove any websites when the answer is yes" in {

      val answers =
        UserAnswers("id")
          .set(WebsitePage(Index(0)), "website 1").success.value
          .set(WebsitePage(Index(1)), "website 2").success.value

      val result = answers.set(HasWebsitePage, true).success.value

      result.get(WebsitePage(Index(0))).value mustEqual "website 1"
      result.get(WebsitePage(Index(1))).value mustEqual "website 2"
    }
  }
}
