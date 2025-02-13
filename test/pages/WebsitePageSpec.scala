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
import models.{AmendMode, CheckMode, Index, NormalMode, RejoinMode}
import pages.behaviours.PageBehaviours

class WebsitePageSpec extends SpecBase with PageBehaviours {

  val index: Index = Index(0)

  "WebsitePage" - {

    beRetrievable[String](WebsitePage(index))

    beSettable[String](WebsitePage(index))

    beRemovable[String](WebsitePage(index))

    "must navigate in Normal mode" - {

      "to Add Website" in {

        WebsitePage(index).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add Website" in {

        WebsitePage(index).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(CheckMode))
      }
    }

    "must navigate in Amend mode" - {

      "to Add Website" in {

        WebsitePage(index).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(AmendMode))
      }
    }

    "must navigate in Rejoin mode" - {

      "to Add Website" in {

        WebsitePage(index).navigate(RejoinMode, emptyUserAnswers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(RejoinMode))
      }
    }

    "must remove all websites if user answers are empty" in {

      val answers = emptyUserAnswers

      val result = answers.get(WebsitePage(Index(0)))

      result must not be defined
    }

    "must not remove all websites if user answers are defined" in {

      val answers = emptyUserAnswers
        .set(WebsitePage(Index(0)), "foo").success.value

      val result = answers.get(WebsitePage(Index(0)))

      result mustBe Some("foo")
    }
  }
}
