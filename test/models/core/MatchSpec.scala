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

package models.core

import base.SpecBase
import play.api.libs.json.{JsSuccess, Json}

import java.time.LocalDate

class MatchSpec extends SpecBase {

  "Match" - {

    "must serialise and deserialise to and from a Match" - {

      "with all optional fields present" in {

        val aMatch: Match =
          Match(
            MatchType.FixedEstablishmentQuarantinedNETP,
            "IM0987654321",
            Some("444444444"),
            "DE",
            Some(3),
            Some(LocalDate.now()),
            Some(LocalDate.now()),
            Some(1),
            Some(2)
          )

        val expectedJson = Json.obj(
          "matchType" -> "006",
          "traderId" -> "IM0987654321",
          "intermediary" -> "444444444",
          "memberState" -> "DE",
          "exclusionStatusCode" -> 3,
          "exclusionDecisionDate" -> s"${LocalDate.now()}",
          "exclusionEffectiveDate" -> s"${LocalDate.now()}",
          "nonCompliantReturns" -> 1,
          "nonCompliantPayments" -> 2
        )

        Json.toJson(aMatch) mustEqual expectedJson
        expectedJson.validate[Match] mustEqual JsSuccess(aMatch)
      }

      "with all optional fields missing" in {

        val aMatch: Match =
          Match(
            MatchType.FixedEstablishmentQuarantinedNETP,
            "IM0987654321",
            None,
            "DE",
            None,
            None,
            None,
            None,
            None
          )

        val expectedJson = Json.obj(
          "matchType" -> "006",
          "traderId" -> "IM0987654321",
          "memberState" -> "DE"
        )

        Json.toJson(aMatch) mustEqual expectedJson
        expectedJson.validate[Match] mustEqual JsSuccess(aMatch)
      }
    }
  }
}
