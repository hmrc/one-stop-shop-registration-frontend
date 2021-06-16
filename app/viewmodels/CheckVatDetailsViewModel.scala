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
import models.Country
import models.domain.VatCustomerInfo
import play.api.i18n.Messages
import play.twirl.api.{Html, HtmlFormat}
import uk.gov.hmrc.domain.Vrn


case class CheckVatDetailsViewModel(vrn: Vrn, vatCustomerInfo: VatCustomerInfo)(implicit messages: Messages) {

  val organisationName: Option[String] = vatCustomerInfo.organisationName.map(HtmlFormat.escape).map(_.toString)

  val formattedDate: Option[String] = vatCustomerInfo.registrationDate.map(_.format(dateFormatter))

  private val country: Option[Country] = Country.allCountries.find(_.code == vatCustomerInfo.address.countryCode)

  val formattedAddress: Html = Html(
    Seq(
      Some(HtmlFormat.escape(vatCustomerInfo.address.line1)),
      vatCustomerInfo.address.line2.map(HtmlFormat.escape),
      vatCustomerInfo.address.line3.map(HtmlFormat.escape),
      vatCustomerInfo.address.line4.map(HtmlFormat.escape),
      vatCustomerInfo.address.line5.map(HtmlFormat.escape),
      vatCustomerInfo.address.postCode.map(HtmlFormat.escape),
      country.map(_.name)
    ).flatten.mkString("<br/>")
  )

  val partOfVatGroup: Option[String] = vatCustomerInfo.partOfVatGroup.map {
    case true  => messages("site.yes")
    case false => messages("site.no")
  }
}
