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

package models.domain

import models.domain.VatDetailSource.{Etmp, UserEntered}
import models.{Address, Enumerable, WithName}
import play.api.libs.json.{Json, OFormat}

import java.time.LocalDate

sealed trait VatDetailSource

object VatDetailSource extends Enumerable.Implicits {
  case object Etmp        extends WithName("etmp") with VatDetailSource
  case object UserEntered extends WithName("userEntered") with VatDetailSource

  val values: Seq[VatDetailSource] = Seq(
    Etmp, UserEntered
  )

  implicit val enumerable: Enumerable[VatDetailSource] =
    Enumerable(values.map(v => v.toString -> v): _*)
}

case class VatDetails private(
                               registrationDate: LocalDate,
                               address: DesAddress,
                               partOfVatGroup: Boolean,
                               source: VatDetailSource
                             )

object VatDetails {

  def apply(registrationDate: LocalDate, address: Address, partOfVatGroup: Boolean): VatDetails =
    VatDetails(
      registrationDate = registrationDate,
      address          = DesAddress(
                           address.line1,
                           address.line2,
                           Some(address.townOrCity),
                           address.county,
                           address.postCode
                         ),
      partOfVatGroup   = partOfVatGroup,
      source           = UserEntered
    )

  def apply(vatInfo: VatCustomerInfo, partOfVatGroup: Boolean): VatDetails =
    VatDetails(
      registrationDate = vatInfo.registrationDate,
      address          = vatInfo.address,
      partOfVatGroup   = partOfVatGroup,
      source           = Etmp
    )

  implicit val format: OFormat[VatDetails] = Json.format[VatDetails]
}
