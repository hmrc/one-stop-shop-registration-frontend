/*
 * Copyright 2022 HM Revenue & Customs
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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.mvc.JavascriptLiteral

class ModeSpec extends AnyFreeSpec with Matchers {

  "Mode" - {

    val literal = implicitly[JavascriptLiteral[Mode]]

    "must convert as a JsLiteral" - {

      "from NormalMode" in {

        literal.to(NormalMode) mustEqual "NormalMode"
      }

      "from CheckMode" in {

        literal.to(CheckMode) mustEqual "CheckMode"
      }

      "from CheckLoopMode" in {

        literal.to(CheckLoopMode) mustEqual "CheckLoopMode"
      }
    }
  }
}
