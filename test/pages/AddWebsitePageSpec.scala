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
import models.{Index, NormalMode}
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
    }
  }
}
