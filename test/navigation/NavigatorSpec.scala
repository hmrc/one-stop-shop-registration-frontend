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

package navigation

import base.SpecBase
import controllers.routes
import pages._
import models._

class NavigatorSpec extends SpecBase {

  val navigator = new Navigator

  "Navigator" - {

    "in Normal mode" - {

      "must go from a page that doesn't exist in the route map to Index" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, NormalMode, UserAnswers("id")) mustBe routes.IndexController.onPageLoad()
      }

      "must go from Registered Company Name to Has Trading Name" in {

        navigator.nextPage(RegisteredCompanyNamePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.HasTradingNameController.onPageLoad(NormalMode))
      }

      "must go from Has Trading Name" - {

        "to Trading Name when the user answers true" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, true).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(routes.TradingNameController.onPageLoad(NormalMode))
        }

        "to Part of VAT Group when the user answers false" in {

          val answers = emptyUserAnswers.set(HasTradingNamePage, false).success.value

          navigator.nextPage(HasTradingNamePage, NormalMode, answers)
            .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
        }
      }

      "must go from Trading Name to Part of VAT Group" in {

        navigator.nextPage(TradingNamePage, NormalMode, emptyUserAnswers)
          .mustBe(routes.PartOfVatGroupController.onPageLoad(NormalMode))
      }
    }

    "in Check mode" - {

      "must go from a page that doesn't exist in the edit route map to CheckYourAnswers" in {

        case object UnknownPage extends Page
        navigator.nextPage(UnknownPage, CheckMode, UserAnswers("id")) mustBe routes.CheckYourAnswersController.onPageLoad()
      }
    }
  }
}
