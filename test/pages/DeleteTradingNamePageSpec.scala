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
import models.{CheckMode, Index, NormalMode}

class DeleteTradingNamePageSpec extends SpecBase {

  "DeleteTradingNamePage" - {

    "must navigate in Normal mode" - {

      "to Add Trading Name when there are still trading names present" in {

        val answers = emptyUserAnswers.set(TradingNamePage(Index(0)), "foo").success.value

        DeleteTradingNamePage(Index(0)).navigate(NormalMode, answers)
          .mustEqual(routes.AddTradingNameController.onPageLoad(NormalMode))
      }

      "to Has Trading Name when there are no trading names present" in {

        DeleteTradingNamePage(Index(0)).navigate(NormalMode, emptyUserAnswers)
          .mustEqual(routes.HasTradingNameController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add Trading Name when there are still trading names present" in {

        val answers = emptyUserAnswers.set(TradingNamePage(Index(0)), "foo").success.value

        DeleteTradingNamePage(Index(0)).navigate(CheckMode, answers)
          .mustEqual(routes.AddTradingNameController.onPageLoad(CheckMode))
      }

      "to Has Trading Name when there are no trading names present" in {

        DeleteTradingNamePage(Index(0)).navigate(CheckMode, emptyUserAnswers)
          .mustEqual(routes.HasTradingNameController.onPageLoad(CheckMode))
      }
    }
  }
}
