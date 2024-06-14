/*
 * Copyright 2024 HM Revenue & Customs
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

package models.domain

import models.{BankDetails, BusinessContactDetails}
import play.api.libs.json.{Json, OFormat}
import uk.gov.hmrc.domain.Vrn
import ModelHelpers._
import models.exclusions.{ExcludedTrader, ExclusionDetails}

import java.time.{Instant, LocalDate}

final case class Registration(
                               vrn: Vrn,
                               registeredCompanyName: String,
                               tradingNames: Seq[String],
                               vatDetails: VatDetails,
                               euRegistrations: Seq[EuTaxRegistration],
                               contactDetails: BusinessContactDetails,
                               websites: Seq[String],
                               commencementDate: LocalDate,
                               previousRegistrations: Seq[PreviousRegistration],
                               bankDetails: BankDetails,
                               isOnlineMarketplace: Boolean,
                               niPresence: Option[NiPresence],
                               dateOfFirstSale: Option[LocalDate],
                               submissionReceived: Option[Instant] = None,
                               adminUse: AdminUse = AdminUse(None),
                               exclusionDetails: Option[ExclusionDetails] = None,
                               excludedTrader: Option[ExcludedTrader] = None,
                             )

object Registration {

  implicit val format: OFormat[Registration] = Json.format[Registration]

  def apply(vrn: Vrn,
            registeredCompanyName: String,
            tradingNames: Seq[String],
            vatDetails: VatDetails,
            euRegistrations: Seq[EuTaxRegistration],
            contactDetails: BusinessContactDetails,
            websites: Seq[String],
            commencementDate: LocalDate,
            previousRegistrations: Seq[PreviousRegistration],
            bankDetails: BankDetails,
            isOnlineMarketplace: Boolean,
            niPresence: Option[NiPresence],
            dateOfFirstSale: Option[LocalDate],
            submissionReceived: Option[Instant] = None,
            adminUse: AdminUse = AdminUse(None),
            exclusionDetails: Option[ExclusionDetails] = None,
            excludedTrader: Option[ExcludedTrader] = None,
           ): Registration = new Registration(
    vrn,
    normaliseSpaces(registeredCompanyName),
    tradingNames.map(normaliseSpaces),
    vatDetails,
    euRegistrations,
    contactDetails,
    websites,
    commencementDate,
    previousRegistrations,
    bankDetails,
    isOnlineMarketplace,
    niPresence,
    dateOfFirstSale,
    submissionReceived,
    adminUse,
    exclusionDetails,
    excludedTrader
  )

}
