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
import models.{AmendMode, CheckMode, Index, NormalMode}

class DeleteWebsitePageSpec extends SpecBase {

  "DeleteWebsitePage" - {

    "must navigate in Normal mode" - {

      "to Add Website when there are still websites present" in {

        val answers = emptyUserAnswers.set(WebsitePage(Index(0)), "foo").success.value

        DeleteWebsitePage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(NormalMode))
      }

      "to Has Website when there are no websites present" in {

        DeleteWebsitePage(Index(0)).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.HasWebsiteController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add Website when there are still websites present" in {

        val answers = emptyUserAnswers.set(WebsitePage(Index(0)), "foo").success.value

        DeleteWebsitePage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(CheckMode))
      }

      "to Has Website when there are no websites present" in {

        DeleteWebsitePage(Index(0)).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.HasWebsiteController.onPageLoad(CheckMode))
      }
    }

    "must navigate in Amend mode" - {

      "to Add Website when there are still websites present" in {

        val answers = emptyUserAnswers.set(WebsitePage(Index(0)), "foo").success.value

        DeleteWebsitePage(Index(0)).navigate(AmendMode, answers)
          .mustEqual(routes.AddWebsiteController.onPageLoad(AmendMode))
      }

      "to Has Website when there are no websites present" in {

        DeleteWebsitePage(Index(0)).navigate(AmendMode, emptyUserAnswers)
          .mustEqual(routes.HasWebsiteController.onPageLoad(AmendMode))
      }
    }

  }
}
