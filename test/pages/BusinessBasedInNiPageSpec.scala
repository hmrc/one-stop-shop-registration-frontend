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
import pages.behaviours.PageBehaviours

class BusinessBasedInNiPageSpec extends SpecBase with PageBehaviours {

  "BusinessBasedInNiPage" - {

    beRetrievable[Boolean](BusinessBasedInNiPage)

    beSettable[Boolean](BusinessBasedInNiPage)

    beRemovable[Boolean](BusinessBasedInNiPage)

    "must navigate" - {

      "to Liable for VAT on All Sales when the answer is yes" in {

        BusinessBasedInNiPage.navigate(true) mustEqual routes.LiableForVatOnAllSalesController.onPageLoad()
      }

      "to Has Fixed Establishment in NI when the answer is no" in {

        BusinessBasedInNiPage.navigate(false) mustEqual routes.HasFixedEstablishmentInNiController.onPageLoad()
      }
    }
  }
}
