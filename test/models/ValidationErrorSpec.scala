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

import base.SpecBase
import org.scalatest.concurrent.ScalaFutures
import play.api.libs.json.JsPath
import queries.Gettable

class ValidationErrorSpec extends SpecBase with ScalaFutures {

  "ValidationError" - {

    "DataMissingError should return correct errorMessage" in {
      val mockPage = new Gettable[String] {
        override def path: JsPath = JsPath \ "mock" \ "path"
      }

      val error = DataMissingError(mockPage)
      error.errorMessage mustBe "Data missing at /mock/path"
    }

    "GenericError should return correct errorMessage" in {
      val error = GenericError("Something went wrong")
      error.errorMessage mustBe "Generic Error Something went wrong"
    }
  }
}
