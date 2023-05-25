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

package models.emails

import play.api.libs.json.{Json, Reads, Writes}

sealed trait EmailParameters

object EmailParameters {
  implicit val reads: Reads[EmailParameters] = Json.reads[EmailParameters]
  implicit val writes: Writes[EmailParameters] = Writes[EmailParameters] {
  case registration: RegistrationConfirmation =>
      Json.toJson(registration)(RegistrationConfirmation.writes)
  case amendRegistration: AmendRegistrationConfirmation =>
      Json.toJson(amendRegistration)(AmendRegistrationConfirmation.writes)
  }
}

case class RegistrationConfirmation(
                                     recipientName_line1: String,
                                     businessName: String,
                                     periodOfFirstReturn: String,
                                     firstDayOfNextPeriod: String,
                                     commencementDate: String,
                                     redirectLink: String
                                   ) extends EmailParameters

object RegistrationConfirmation {
  implicit val reads: Reads[RegistrationConfirmation] = Json.reads[RegistrationConfirmation]
  implicit val writes: Writes[RegistrationConfirmation] = Json.writes[RegistrationConfirmation]
}

case class AmendRegistrationConfirmation(
                                          recipientName_line1: String,
                                          amendmentDate: String
                                        ) extends EmailParameters

object AmendRegistrationConfirmation {

  implicit val reads: Reads[AmendRegistrationConfirmation] = Json.reads[AmendRegistrationConfirmation]
  implicit val writes: Writes[AmendRegistrationConfirmation] = Json.writes[AmendRegistrationConfirmation]
}


