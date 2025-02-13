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

package models.audit

import base.SpecBase
import models.requests.AuthenticatedDataRequest
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import testutils.RegistrationData

class RegistrationAuditModelSpec extends SpecBase {

  private val registrationAuditType: RegistrationAuditType = RegistrationAuditType.CreateRegistration
  private val submissionResult: SubmissionResult = SubmissionResult.values.head
  private val registration = RegistrationData.registration

  "RegistrationAuditModelSpec" - {

    "must create correct json object" in {

      val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, None, emptyUserAnswers)

      implicit val dataRequest: AuthenticatedDataRequest[AnyContent] =
        AuthenticatedDataRequest(request, testCredentials, vrn, None, emptyUserAnswers)

      val registrationAuditModel = RegistrationAuditModel.build(
        registrationAuditType = registrationAuditType,
        registration = registration,
        result = submissionResult,
        dataRequest
      )

      val expectedJson = Json.obj(
        "credId" -> request.credentials.providerId,
        "browserUserAgent" -> "",
        "submissionResult" -> submissionResult,
        "registrationDetails" -> Json.obj(
          "vatRegistrationNumber" -> registration.vrn,
          "registeredCompanyName" -> registration.registeredCompanyName,
          "vatDetails" -> Json.toJson(registration.vatDetails),
          "contactDetails" -> Json.toJson(registration.contactDetails),
          "commencementDate" -> Json.toJson(registration.commencementDate),
          "bankDetails" -> Json.toJson(registration.bankDetails),
          "isOnlineMarketplace" -> registration.isOnlineMarketplace,
          "tradingNames" -> registration.tradingNames,
          "euRegistrations" -> Json.toJson(registration.euRegistrations),
          "websites" -> registration.websites,
          "previousRegistrations" -> Json.toJson(registration.previousRegistrations),
          "niPresence" -> registration.niPresence,
          "dateOfFirstSale" -> registration.dateOfFirstSale
        )
      )

      registrationAuditModel.detail mustBe expectedJson
    }
  }
}
