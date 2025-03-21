/*
 * Copyright 2025 HM Revenue & Customs
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

package models.iossRegistration

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class IossEtmpTradingNameSpec extends SpecBase {

  private val etmpTradingName = arbitraryIossEtmpTradingName.arbitrary.sample.value

  "IossEtmpTradingName" - {

    "must serialise/deserialise to and from IossEtmpTradingName" in {

      val expectedJson = Json.obj(
        "tradingName" -> s"${etmpTradingName.tradingName}"
      )

      Json.toJson(etmpTradingName) mustBe expectedJson
      expectedJson.validate[IossEtmpTradingName] mustBe JsSuccess(etmpTradingName)
    }

    "must handle empty data object" in {

      val json = Json.obj()

      json.validate[IossEtmpTradingName] mustBe a[JsError]
    }
  }
}
