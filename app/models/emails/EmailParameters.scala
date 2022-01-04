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

package models.emails

import play.api.libs.json.{Json, Reads, Writes}

sealed trait EmailParameters

object EmailParameters {
  implicit val writes: Writes[EmailParameters] = Writes[EmailParameters] {
    case pre10th: RegistrationConfirmationEmailPre10thParameters =>
      Json.toJson(pre10th)(RegistrationConfirmationEmailPre10thParameters.writes)
    case post10th: RegistrationConfirmationEmailPost10thParameters =>
      Json.toJson(post10th)(RegistrationConfirmationEmailPost10thParameters.writes)
  }

  implicit val reads: Reads[EmailParameters] = Json.reads[EmailParameters]
}

case class RegistrationConfirmationEmailPre10thParameters(
  recipientName_line1: String,
  businessName:String,
  startDate: String,
  reference: String,
  lastDayOfCalendarQuarter: String,
  lastDayOfMonthAfterCalendarQuarter: String
) extends EmailParameters

object RegistrationConfirmationEmailPre10thParameters {
  implicit val writes: Writes[RegistrationConfirmationEmailPre10thParameters] =
    Json.writes[RegistrationConfirmationEmailPre10thParameters]
  implicit val reads: Reads[RegistrationConfirmationEmailPre10thParameters] =
    Json.reads[RegistrationConfirmationEmailPre10thParameters]
}

case class RegistrationConfirmationEmailPost10thParameters(
  recipientName_line1: String,
  businessName:String,
  startDate: String,
  reference: String,
  lastDayOfCalendarQuarter: String,
  lastDayOfMonthAfterNextCalendarQuarter: String,
  firstDayOfNextCalendarQuarter: String,
  lastDayOfNextCalendarQuarter: String
) extends EmailParameters

object RegistrationConfirmationEmailPost10thParameters {
  implicit val writes: Writes[RegistrationConfirmationEmailPost10thParameters] =
    Json.writes[RegistrationConfirmationEmailPost10thParameters]
  implicit val reads: Reads[RegistrationConfirmationEmailPost10thParameters] =
    Json.reads[RegistrationConfirmationEmailPost10thParameters]
}
