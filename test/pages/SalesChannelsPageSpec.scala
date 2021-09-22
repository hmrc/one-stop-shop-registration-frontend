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
import models.SalesChannels
import models.SalesChannels._
import pages.behaviours.PageBehaviours

class SalesChannelsSpec extends SpecBase with PageBehaviours {

  "SalesChannelsPage" - {

    beRetrievable[SalesChannels](SalesChannelsPage)

    beSettable[SalesChannels](SalesChannelsPage)

    beRemovable[SalesChannels](SalesChannelsPage)


    "must navigate" - {

      "to Not Liable For VAT when the answer is Online Marketplaces" in {

        SalesChannelsPage.navigate(OnlineMarketplaces) mustEqual routes.NotLiableForVatController.onPageLoad()
      }

      "to Liable for VAT on Direct Sales when the answer Mixed" in {

        SalesChannelsPage.navigate(Mixed) mustEqual routes.LiableForVatOnDirectSalesController.onPageLoad()
      }

      "to Liable for VAT on Direct Sales when the answer Not Online Marketplaces" in {

        SalesChannelsPage.navigate(NotOnlineMarketplaces) mustEqual routes.LiableForVatOnAllSalesController.onPageLoad()
      }
    }
  }
}
