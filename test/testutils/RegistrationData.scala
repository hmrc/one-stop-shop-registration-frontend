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

package testutils

import models.requests.RegistrationRequest
import models.{Address, BusinessContactDetails, Country, EuVatDetails, StartDate, StartDateOption}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

object RegistrationData {
  def createNewRegistration(): RegistrationRequest =
    RegistrationRequest(
      "foo",
      true,
      List("single", "double"),
      true,
      Vrn("GB123456789"),
      LocalDate.now(),
      "AA1 1AA",
      true,
      Seq(
        EuVatDetails(Country("FR", "France"),"FR123456789", false, None, None),
        EuVatDetails(Country("ES", "Spain"),"ES123456789", false, None, None)
      ),
      LocalDate.now(),
      createBusinessAddress(),
      createBusinessContactDetails(),
      Seq("website1", "website2")
    )

  val registration: RegistrationRequest = createNewRegistration()

  def createRegistrationRequest(): RegistrationRequest =
    RegistrationRequest(
      registration.registeredCompanyName,
      registration.hasTradingName,
      registration.tradingNames,
      registration.partOfVatGroup,
      registration.ukVatNumber,
      registration.ukVatEffectiveDate,
      registration.ukVatRegisteredPostcode,
      registration.vatRegisteredInEu,
      registration.euVatDetails,
      registration.startDate,
      registration.businessAddress,
      registration.businessContactDetails,
      registration.websites
    )

  private def createBusinessAddress(): Address =
    Address(
      "123 Street",
      Some("Street"),
      "City",
      Some("county"),
      "AA12 1AB"
    )

  private def createBusinessContactDetails(): BusinessContactDetails =
    BusinessContactDetails(
      "Joe Bloggs",
      "01112223344",
      "email@email.com"
    )
}

