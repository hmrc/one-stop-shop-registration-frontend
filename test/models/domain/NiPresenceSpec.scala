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

package models.domain

import generators.Generators
import models.SalesChannels
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json._

class NiPresenceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with OptionValues {

  "NI Presence" - {

    "must serialise and deserialise to / from PPOB in NI" in {

      val json = JsString("principalPlaceOfBusinessInNi")
      json.validate[NiPresence] mustEqual JsSuccess(PrincipalPlaceOfBusinessInNi)
      Json.toJson(PrincipalPlaceOfBusinessInNi: NiPresence) mustEqual json
    }

    "must serialise and deserialise to / from FE in NI" in {

      val json = JsString("fixedEstablishmentInNi")
      json.validate[NiPresence] mustEqual JsSuccess(FixedEstablishmentInNi)
      Json.toJson(FixedEstablishmentInNi: NiPresence) mustEqual json
    }

    "must serialise and deserialise to / from No Presence" in {

      forAll(arbitrary[SalesChannels]) {
        salesChannels =>
          val json = Json.obj(
            "presence" -> "noPresence",
            "salesChannels" -> salesChannels
          )

          json.validate[NiPresence] mustEqual JsSuccess(NoPresence(salesChannels))
          Json.toJson(NoPresence(salesChannels): NiPresence) mustEqual json
      }
    }
  }
}
