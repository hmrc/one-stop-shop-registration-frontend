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

package models.requests

import models.{BusinessAddress, BusinessContactDetails, EuVatDetails}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

final case class RegistrationRequest(
  registeredCompanyName: String,
  hasTradingName: Boolean,
  tradingNames: List[String],
  partOfVatGroup: Boolean,
  ukVatNumber: Vrn,
  ukVatEffectiveDate: LocalDate,
  ukVatRegisteredPostcode: String,
  vatRegisteredInEu: Boolean,
  euVatDetails: Seq[EuVatDetails],
  businessAddress: BusinessAddress,
  businessContactDetails: BusinessContactDetails,
  websites: Seq[String]
)

case object RegistrationRequest {
  implicit val format: OFormat[RegistrationRequest] = Json.format[RegistrationRequest]
}

