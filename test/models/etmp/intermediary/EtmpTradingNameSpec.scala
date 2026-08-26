/*
 * Copyright 2026 HM Revenue & Customs
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

package models.etmp.intermediary

import base.SpecBase
import play.api.libs.json.{JsError, JsSuccess, Json}

class EtmpTradingNameSpec extends SpecBase {

  private val etmpTradingName: EtmpTradingName = arbitraryEtmpTradingName.arbitrary.sample.value

  "EtmpTradingName" - {

    "must deserialise/serialise from and to EtmpTradingName" in {

      val json = Json.obj(
        "tradingName" -> etmpTradingName.tradingName
      )

      val expectedResult: EtmpTradingName = EtmpTradingName(
        tradingName = etmpTradingName.tradingName
      )

      Json.toJson(expectedResult) `mustBe` json
      json.validate[EtmpTradingName] `mustBe` JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[EtmpTradingName] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "tradingName" -> 123456
      )

      json.validate[EtmpTradingName] `mustBe` a[JsError]
    }
  }
}
