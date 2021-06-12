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

package models

import generators.Generators
import models.CurrentlyRegisteredInCountry.{No, Yes}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.libs.json.{JsSuccess, Json}

class CurrentlyRegisteredInCountrySpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  "CurrentlyRegisteredInCountry" - {

    "must serialise / deserialise from and to Yes" in {

      forAll(arbitrary[Country]) {
        country =>
          Json.toJson(Yes(country): CurrentlyRegisteredInCountry).validate[CurrentlyRegisteredInCountry] mustEqual JsSuccess(Yes(country))
      }
    }

    "must serialise / deserialise from and to No" in {

      Json.toJson(No: CurrentlyRegisteredInCountry).validate[CurrentlyRegisteredInCountry] mustEqual JsSuccess(No)
    }


    ".asBoolean" - {

      "must be true for Yes" in {

        forAll(arbitrary[Country]) {
          country =>
            Yes(country).asBoolean mustEqual true
        }
      }

      "must be false for No" in {

        No.asBoolean mustEqual false
      }
    }
  }
}
