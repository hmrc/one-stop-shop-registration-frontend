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

import models.Country
import play.api.libs.json.{Json, OFormat, Reads, Writes}

sealed trait EuTaxRegistration {
  def country: Country
}

object EuTaxRegistration {

  implicit val reads: Reads[EuTaxRegistration] =
    RegistrationWithFixedEstablishment.format.widen[EuTaxRegistration] orElse
    RegistrationWithoutFixedEstablishmentWithTradeDetails.format.widen[EuTaxRegistration] orElse
      EuVatRegistration.format.widen[EuTaxRegistration] orElse
      RegistrationWithoutTaxId.format.widen[EuTaxRegistration]


  implicit val writes: Writes[EuTaxRegistration] = Writes {
    case v: EuVatRegistration                     => Json.toJson(v)(EuVatRegistration.format)
    case fe: RegistrationWithFixedEstablishment   => Json.toJson(fe)(RegistrationWithFixedEstablishment.format)
    case fe: RegistrationWithoutFixedEstablishmentWithTradeDetails   => Json.toJson(fe)(RegistrationWithoutFixedEstablishmentWithTradeDetails.format)
    case w: RegistrationWithoutTaxId => Json.toJson(w)(RegistrationWithoutTaxId.format)
  }
}

final case class EuVatRegistration(
                                    country: Country,
                                    vatNumber: String
                                  ) extends EuTaxRegistration

object EuVatRegistration {

  implicit val format: OFormat[EuVatRegistration] =
    Json.format[EuVatRegistration]
}

final case class RegistrationWithFixedEstablishment(
                                                    country: Country,
                                                    taxIdentifier: EuTaxIdentifier,
                                                    fixedEstablishment: TradeDetails
                                                  ) extends EuTaxRegistration

object RegistrationWithFixedEstablishment {
  implicit val format: OFormat[RegistrationWithFixedEstablishment] =
    Json.format[RegistrationWithFixedEstablishment]
}

final case class RegistrationWithoutFixedEstablishmentWithTradeDetails(
                                                        country: Country,
                                                        taxIdentifier: EuTaxIdentifier,
                                                        tradeDetails: TradeDetails
                                                      ) extends EuTaxRegistration

object RegistrationWithoutFixedEstablishmentWithTradeDetails {

  implicit val format: OFormat[RegistrationWithoutFixedEstablishmentWithTradeDetails] =
    Json.format[RegistrationWithoutFixedEstablishmentWithTradeDetails]
}

final case class RegistrationWithoutTaxId(country: Country) extends EuTaxRegistration

object RegistrationWithoutTaxId {
  implicit val format: OFormat[RegistrationWithoutTaxId] =
    Json.format[RegistrationWithoutTaxId]
}
