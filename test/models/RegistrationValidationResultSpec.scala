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

package models

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.libs.json.Json

class RegistrationValidationResultSpec extends AnyFreeSpec with Matchers {

  "RegistrationValidationResult" - {

    "serialize to JSON correctly" in {
      val registrationValidationResult = RegistrationValidationResult(validRegistration = true)

      val expectedJson = Json.obj("validRegistration" -> true)

      Json.toJson(registrationValidationResult) mustBe expectedJson
    }

    "deserialize from JSON correctly" in {
      val json = Json.obj("validRegistration" -> false)
      val expectedResult = RegistrationValidationResult(validRegistration = false)


      json.as[RegistrationValidationResult] mustBe expectedResult
    }

    "handle invalid JSON" in {
      val invalidJson = Json.obj(
        "unexpectedField" -> "value",
      )

      val result = invalidJson.validate[RegistrationValidationResult]
      result.isError mustBe true
    }
  }
}
