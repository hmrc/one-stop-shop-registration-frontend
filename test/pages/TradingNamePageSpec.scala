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
import pages.behaviours.PageBehaviours


class TradingNamePageSpec extends SpecBase with PageBehaviours {

  val index: Index = Index(0)

  "TradingNamePage" - {

    beRetrievable[String](TradingNamePage(index))

    beSettable[String](TradingNamePage(index))

    beRemovable[String](TradingNamePage(index))

    "must navigate in Normal mode" - {

      "to Add Trading Name" in {

        TradingNamePage(index).navigate(NormalMode, emptyUserAnswers)
          .mustBe(routes.AddTradingNameController.onPageLoad(NormalMode))
      }
    }

    "must navigate in Check mode" - {

      "to Add Trading Name" in {

        TradingNamePage(index).navigate(CheckMode, emptyUserAnswers)
          .mustBe(routes.AddTradingNameController.onPageLoad(CheckMode))
      }
    }

    "must navigate in Amend mode" - {

      "to Add Trading Name" in {

        TradingNamePage(index).navigate(AmendMode, emptyUserAnswers)
          .mustBe(routes.AddTradingNameController.onPageLoad(AmendMode))
      }
    }

    "must remove all trading names if user answers are empty" in {

      val answers = emptyUserAnswers

      val result = answers.get(TradingNamePage(Index(0)))

      result must not be defined
    }

    "must not remove all trading names if user answers are defined" in {

      val answers = emptyUserAnswers
        .set(TradingNamePage(Index(0)), "foo trading name").success.value

      val result = answers.get(TradingNamePage(Index(0)))

      result mustBe Some("foo trading name")
    }
  }
}
