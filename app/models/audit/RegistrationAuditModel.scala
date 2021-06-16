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
import models.requests.DataRequest
import play.api.libs.json.{JsValue, Json}

case class RegistrationAuditModel(
                                   credId: String,
                                   userAgent: String,
                                   registration: Registration,
                                   result: SubmissionResult
                                 ) extends JsonAuditModel {

  override val auditType: String       = "RegistrationSubmitted"
  override val transactionName: String = "registration-submitted"

  override val detail: JsValue = Json.obj(
    "credId"              -> credId,
    "userAgent"           -> userAgent,
    "submissionResult"    -> Json.toJson(result),
    "registrationDetails" -> Json.toJson(registration)
  )
}

object RegistrationAuditModel {

  def build(
             registration: Registration,
             result: SubmissionResult,
             request: DataRequest[_]
           ): RegistrationAuditModel =
    RegistrationAuditModel(
      credId = request.credentials.providerId,
      userAgent = request.headers.get("user-agent").getOrElse(""),
      registration = registration,
      result = result
    )
}