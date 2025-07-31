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

import models.domain.ModelHelpers.*
import models.exclusions.{ExcludedTrader, ExclusionDetails}
import models.{BankDetails, BusinessContactDetails}
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.{Json, Reads, Writes, __}
import uk.gov.hmrc.domain.Vrn

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
                               rejoin: Option[Boolean] = None,
                               unusableStatus: Option[Boolean] = None,
                               nonCompliantReturns: Option[String] = None,
                               nonCompliantPayments: Option[String] = None
                             )

object Registration {

  implicit val reads: Reads[Registration] = (
    (__ \ "vrn").read[Vrn] and
      (__ \ "registeredCompanyName").read[String].map(normaliseSpaces) and
      (__ \ "tradingNames").read[Seq[String]].map(_.map(normaliseSpaces)) and
      (__ \ "vatDetails").read[VatDetails] and
      (__ \ "euRegistrations").read[Seq[EuTaxRegistration]] and
      (__ \ "contactDetails").read[BusinessContactDetails] and
      (__ \ "websites").read[Seq[String]] and
      (__ \ "commencementDate").read[LocalDate] and
      (__ \ "previousRegistrations").read[Seq[PreviousRegistration]] and
      (__ \ "bankDetails").read[BankDetails] and
      (__ \ "isOnlineMarketplace").read[Boolean] and
      (__ \ "niPresence").readNullable[NiPresence] and
      (__ \ "dateOfFirstSale").readNullable[LocalDate] and
      (__ \ "submissionReceived").readNullable[Instant] and
      (__ \ "adminUse").read[AdminUse] and
      (__ \ "exclusionDetails").readNullable[ExclusionDetails] and
      (__ \ "excludedTrader").readNullable[ExcludedTrader] and
      (__ \ "rejoin").readNullable[Boolean] and
      (__ \ "unusableStatus").readNullable[Boolean] and
      (__ \ "nonCompliantReturns").readNullable[String] and
      (__ \ "nonCompliantPayments").readNullable[String]
    )(Registration.apply _)

  implicit val writes: Writes[Registration] = Json.writes[Registration]

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
            rejoin: Option[Boolean] = None,
            unusableStatus: Option[Boolean] = None,
            nonCompliantReturns: Option[String] = None,
            nonCompliantPayments: Option[String] = None
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
    excludedTrader,
    rejoin = rejoin,
    unusableStatus,
    nonCompliantReturns,
    nonCompliantPayments
  )

}
