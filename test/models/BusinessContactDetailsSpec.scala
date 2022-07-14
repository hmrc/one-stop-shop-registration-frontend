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
import play.api.libs.json.Json

class BusinessContactDetailsSpec extends AnyFreeSpec with Matchers {

  "BusinessContactDetails" - {

      "must exclude trailing and leading whitespace and double spaces" in {

        val businessContactDetails = BusinessContactDetails(
          fullName = "      full    name        ",
          telephoneNumber = "012345678",
          emailAddress = "a@b.c"
        )

        val json = Json.obj(
          "fullName"       -> "full name",
          "telephoneNumber"       -> "012345678",
          "emailAddress"  -> "a@b.c"
        )

        json.as[BusinessContactDetails] mustEqual businessContactDetails
      }
    }
}
