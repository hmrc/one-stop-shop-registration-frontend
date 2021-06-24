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
import pages.behaviours.PageBehaviours

class HasFixedEstablishmentInNiPageSpec extends SpecBase with PageBehaviours {

  "HasFixedEstablishmentInNiPage" - {

    beRetrievable[Boolean](HasFixedEstablishmentInNiPage)

    beSettable[Boolean](HasFixedEstablishmentInNiPage)

    beRemovable[Boolean](HasFixedEstablishmentInNiPage)

    "must navigate" - {

      "to Liable For VAT on All Sales when the answer is yes" in {

        HasFixedEstablishmentInNiPage.navigate(true) mustEqual routes.LiableForVatOnAllSalesController.onPageLoad()
      }

      "to All Sales Via Marketplace when the answer is no" in {

        HasFixedEstablishmentInNiPage.navigate(false) mustEqual routes.SalesChannelsController.onPageLoad()
      }
    }
  }
}
