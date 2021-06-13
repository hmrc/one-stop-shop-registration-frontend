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

import models.domain._
import models.{BankDetails, BusinessContactDetails, Country, InternationalAddress, UkAddress}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

object RegistrationData {
  val registration: Registration =
    Registration(
      vrn = Vrn("123456789"),
      registeredCompanyName = "foo",
      tradingNames = List("single", "double"),
      vatDetails = VatDetails(
        registrationDate = LocalDate.now,
        address          = createUkAddress(),
        partOfVatGroup   = true,
        source           = VatDetailSource.Etmp
      ),
      euRegistrations = Seq(
        EuVatRegistration(Country("FR", "France"), "FR123456789"),
        RegistrationWithFixedEstablishment(
          Country("ES", "Spain"),
          EuTaxIdentifier(EuTaxIdentifierType.Vat, "ES123456789"),
          FixedEstablishment("Spanish trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("ES", "Spain")))
        ),
        RegistrationWithFixedEstablishment(
          Country("DE", "Germany"),
          EuTaxIdentifier(EuTaxIdentifierType.Other, "DE123456789"),
          FixedEstablishment("German trading name", InternationalAddress("Line 1", None, "Town", None, None, Country("DE", "Germany")))
        )
      ),
      contactDetails = createBusinessContactDetails(),
      websites = Seq("website1", "website2"),
      startDate = LocalDate.now(),
      currentCountryOfRegistration = Some(Country("FR", "France")),
      previousRegistrations = Seq(
        PreviousRegistration(Country("DE", "Germany"), "DE123")
      ),
      bankDetails = BankDetails("Account name", Some("12345678"), "GB12345678")
    )

  private def createUkAddress(): UkAddress =
    UkAddress(
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

