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

class IossEtmpBankDetailsSpec extends SpecBase {

  private val etmpBankDetails: IossEtmpBankDetails = arbitraryIossEtmpBankDetails.arbitrary.sample.value

  "must deserialise/serialise to and from IossEtmpBankDetails" - {

    "when all optional values are present" in {

      val json = Json.obj(
        "accountName" -> etmpBankDetails.accountName,
        "bic" -> etmpBankDetails.bic,
        "iban" -> etmpBankDetails.iban
      )

      val expectedResult = IossEtmpBankDetails(
        accountName = etmpBankDetails.accountName,
        bic = etmpBankDetails.bic,
        iban = etmpBankDetails.iban
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[IossEtmpBankDetails] mustBe JsSuccess(expectedResult)
    }

    "when all optional values are absent" in {

      val json = Json.obj(
        "accountName" -> etmpBankDetails.accountName,
        "iban" -> etmpBankDetails.iban
      )

      val expectedResult = IossEtmpBankDetails(
        accountName = etmpBankDetails.accountName,
        bic = None,
        iban = etmpBankDetails.iban
      )

      Json.toJson(expectedResult) mustBe json
      json.validate[IossEtmpBankDetails] mustBe JsSuccess(expectedResult)
    }

    "must handle empty data object" in {

      val json = Json.obj()

      json.validate[IossEtmpBankDetails] mustBe a[JsError]
    }
  }
}

