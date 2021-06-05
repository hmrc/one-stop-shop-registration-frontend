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

package viewmodels

import formats.Format.dateFormatter
import models.DesAddress
import models.domain.VatCustomerInfo
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate


case class CheckVatDetailsViewModel(vrn: Vrn, registrationDate: Option[LocalDate], address: DesAddress) {

  def formattedDate: Option[String] = registrationDate.map(_.format(dateFormatter))
}

object CheckVatDetailsViewModel {

  def apply(vrn: Vrn, vatCustomerInfo: VatCustomerInfo): CheckVatDetailsViewModel =
    CheckVatDetailsViewModel(vrn, vatCustomerInfo.registrationDate, vatCustomerInfo.address)
}
