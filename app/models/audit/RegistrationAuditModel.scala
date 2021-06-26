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

package models.audit
import models.domain.Registration
import models.requests.AuthenticatedDataRequest
import play.api.libs.json.{JsObject, JsValue, Json}

case class RegistrationAuditModel(
                                   credId: String,
                                   userAgent: String,
                                   registration: Registration,
                                   result: SubmissionResult
                                 ) extends JsonAuditModel {

  override val auditType: String       = "RegistrationSubmitted"
  override val transactionName: String = "registration-submitted"

  private def previousRegistrationDetail: JsObject =
    if (registration.previousRegistrations.nonEmpty) {
      Json.obj("previousRegistrations" -> Json.toJson(registration.previousRegistrations))
    } else {
      Json.obj()
    }

  private def websiteDetail: JsObject =
    if(registration.websites.nonEmpty) Json.obj("websites" -> registration.websites) else Json.obj()

  private def euRegistrationDetail: JsObject =
    if (registration.euRegistrations.nonEmpty) Json.obj("euRegistrations" -> Json.toJson(registration.euRegistrations)) else Json.obj()

  private val tradingNameDetail: JsObject =
    if (registration.tradingNames.nonEmpty) Json.obj("tradingNames" -> registration.tradingNames) else Json.obj()

  private val registrationDetail: JsValue = Json.obj(
    "vatRegistrationNumber" -> registration.vrn,
    "registeredCompanyName" -> registration.registeredCompanyName,
    "vatDetails"            -> Json.toJson(registration.vatDetails),
    "contactDetails"        -> Json.toJson(registration.contactDetails),
    "commencementDate"      -> Json.toJson(registration.commencementDate),
    "bankDetails"           -> Json.toJson(registration.bankDetails)
  ) ++ tradingNameDetail ++
    euRegistrationDetail ++
    websiteDetail ++
    previousRegistrationDetail

  override val detail: JsValue = Json.obj(
    "credId"              -> credId,
    "browserUserAgent"    -> userAgent,
    "submissionResult"    -> Json.toJson(result),
    "registrationDetails" -> registrationDetail
  )
}

object RegistrationAuditModel {

  def build(
             registration: Registration,
             result: SubmissionResult,
             request: AuthenticatedDataRequest[_]
           ): RegistrationAuditModel =
    RegistrationAuditModel(
      credId = request.credentials.providerId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      registration = registration,
      result = result
    )
}