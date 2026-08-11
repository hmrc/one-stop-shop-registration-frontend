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
import models.domain.VatCustomerInfo
import play.api.libs.functional.syntax.*
import play.api.libs.json.*

class IntermediaryRegistrationWrapperSpec extends SpecBase {

  private val vatCustomerInfo: IntermediaryVatCustomerInfo = arbitraryVatCustomerInfo.arbitrary.sample.value
  private val etmpDisplayRegistration: EtmpIntermediaryDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  private val registrationWrapper: IntermediaryRegistrationWrapper = IntermediaryRegistrationWrapper(
    vatInfo = vatCustomerInfo,
    etmpDisplayRegistration = etmpDisplayRegistration
  )

  "IntermediaryRegistrationWrapper" - {

    "must deserialise from RegistrationWrapper" in {

      val json: JsValue = Json.toJson(registrationWrapper)

      val expectedResult = IntermediaryRegistrationWrapper(
        vatInfo = vatCustomerInfo,
        etmpDisplayRegistration = etmpDisplayRegistration
      )

      Json.toJson(expectedResult) `mustBe` json
    }

    "must serialise to RegistrationWrapper" in {

      val registrationWrapperWrites: Writes[IntermediaryRegistrationWrapper] = {
        (
          (__ \ "vatInfo").write[IntermediaryVatCustomerInfo] and
            (__ \ "etmpDisplayRegistration").write[EtmpIntermediaryDisplayRegistration]
          )(registrationWrapper => Tuple.fromProductTyped(registrationWrapper))
      }

      val json: JsValue = Json.toJson(registrationWrapper)(registrationWrapperWrites)

      val expectedResult = IntermediaryRegistrationWrapper(
        vatInfo = vatCustomerInfo,
        etmpDisplayRegistration = etmpDisplayRegistration
      )

      json.validate[IntermediaryRegistrationWrapper] `mustBe` JsSuccess(expectedResult)
    }

    "must handle missing fields during deserialization" in {

      val json = Json.obj()

      json.validate[IntermediaryRegistrationWrapper] `mustBe` a[JsError]
    }

    "must handle invalid fields during deserialization" in {

      val json = Json.obj(
        "vatInfo" -> 123456
      )

      json.validate[IntermediaryRegistrationWrapper] `mustBe` a[JsError]
    }
  }
}
