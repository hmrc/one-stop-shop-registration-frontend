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

import models.{Index, UserAnswers}
import pages.behaviours.PageBehaviours

class HasTradingNamePageSpec extends PageBehaviours {

  "HasTradingNamePage" - {

    beRetrievable[Boolean](HasTradingNamePage)

    beSettable[Boolean](HasTradingNamePage)

    beRemovable[Boolean](HasTradingNamePage)

    "must remove all trading names when the answer is false" in {

      val answers =
        UserAnswers("id")
          .set(TradingNamePage(Index(0)), "name 1").success.value
          .set(TradingNamePage(Index(1)), "name 2").success.value

      val result = answers.set(HasTradingNamePage, false).success.value

      result.get(TradingNamePage(Index(0))) must not be defined
      result.get(TradingNamePage(Index(1))) must not be defined
    }

    "must not remove any trading names when the answer is true" in {

      val answers =
        UserAnswers("id")
          .set(TradingNamePage(Index(0)), "name 1").success.value
          .set(TradingNamePage(Index(1)), "name 2").success.value

      val result = answers.set(HasTradingNamePage, true).success.value

      result.get(TradingNamePage(Index(0))).value mustEqual "name 1"
      result.get(TradingNamePage(Index(1))).value mustEqual "name 2"
    }
  }
}
