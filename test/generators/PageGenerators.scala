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

package generators

import models.Index
import org.scalacheck.Arbitrary
import pages.{euVatDetails, _}
import pages.euVatDetails.{AddEuVatDetailsPage, EuCountryPage, EuVatNumberPage, FixedEstablishmentAddressPage, FixedEstablishmentTradingNamePage, HasFixedEstablishmentPage, VatRegisteredInEuPage}
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}

trait PageGenerators {

  implicit lazy val arbitraryPreviouslyRegisteredPage: Arbitrary[PreviouslyRegisteredPage.type] =
    Arbitrary(PreviouslyRegisteredPage)

  implicit lazy val arbitraryPreviousEuVatNumberPage: Arbitrary[PreviousEuVatNumberPage] =
    Arbitrary(PreviousEuVatNumberPage(Index(0)))

  implicit lazy val arbitraryPreviousEuCountryPage: Arbitrary[PreviousEuCountryPage] =
    Arbitrary(PreviousEuCountryPage(Index(0)))

  implicit lazy val arbitraryAddPreviousRegistrationPage: Arbitrary[AddPreviousRegistrationPage.type] =
    Arbitrary(AddPreviousRegistrationPage)

  implicit lazy val arbitraryCurrentlyRegisteredInEuPage: Arbitrary[CurrentlyRegisteredInEuPage.type] =
    Arbitrary(CurrentlyRegisteredInEuPage)

  implicit lazy val arbitraryCurrentCountryOfRegistrationPage: Arbitrary[CurrentCountryOfRegistrationPage.type] =
    Arbitrary(CurrentCountryOfRegistrationPage)

  implicit lazy val arbitraryHasFixedEstablishmentPage: Arbitrary[HasFixedEstablishmentPage] =
    Arbitrary(euVatDetails.HasFixedEstablishmentPage(Index(0)))

  implicit lazy val arbitraryFixedEstablishmentTradingNamePage: Arbitrary[FixedEstablishmentTradingNamePage] =
    Arbitrary(euVatDetails.FixedEstablishmentTradingNamePage(Index(0)))

  implicit lazy val arbitraryFixedEstablishmentAddressPage: Arbitrary[FixedEstablishmentAddressPage] =
    Arbitrary(euVatDetails.FixedEstablishmentAddressPage(Index(0)))

  implicit lazy val arbitraryCheckVatDetailsPage: Arbitrary[CheckVatDetailsPage.type] =
    Arbitrary(CheckVatDetailsPage)

  implicit lazy val arbitraryStartDatePage: Arbitrary[StartDatePage.type] =
    Arbitrary(StartDatePage)

  implicit lazy val arbitraryWebsitePage: Arbitrary[WebsitePage] =
    Arbitrary(WebsitePage(Index(0)))

  implicit lazy val arbitraryBusinessContactDetailsPage: Arbitrary[BusinessContactDetailsPage.type] =
    Arbitrary(BusinessContactDetailsPage)

  implicit lazy val arbitraryBusinessAddressPage: Arbitrary[BusinessAddressPage.type] =
    Arbitrary(BusinessAddressPage)

  implicit lazy val arbitraryAddEuVatDetailsPage: Arbitrary[AddEuVatDetailsPage.type] =
    Arbitrary(AddEuVatDetailsPage)

  implicit lazy val arbitraryVatRegisteredInEuPage: Arbitrary[VatRegisteredInEuPage.type] =
    Arbitrary(VatRegisteredInEuPage)

  implicit lazy val arbitraryEuCountryPage: Arbitrary[EuCountryPage] =
    Arbitrary(euVatDetails.EuCountryPage(Index(0)))

  implicit lazy val arbitraryEuVatNumberPage: Arbitrary[EuVatNumberPage] =
    Arbitrary(euVatDetails.EuVatNumberPage(Index(0)))

  implicit lazy val arbitraryUkVatRegisteredPostcodePage: Arbitrary[UkVatRegisteredPostcodePage.type] =
    Arbitrary(UkVatRegisteredPostcodePage)

  implicit lazy val arbitraryUkVatEffectiveDatePage: Arbitrary[UkVatEffectiveDatePage.type] =
    Arbitrary(UkVatEffectiveDatePage)

  implicit lazy val arbitraryTradingNamePage: Arbitrary[TradingNamePage] =
    Arbitrary(TradingNamePage(Index(0)))

  implicit lazy val arbitraryRegisteredCompanyNamePage: Arbitrary[RegisteredCompanyNamePage.type] =
    Arbitrary(RegisteredCompanyNamePage)

  implicit lazy val arbitraryPartOfVatGroupPage: Arbitrary[PartOfVatGroupPage.type] =
    Arbitrary(PartOfVatGroupPage)

  implicit lazy val arbitraryHasTradingNamePage: Arbitrary[HasTradingNamePage.type] =
    Arbitrary(HasTradingNamePage)
}
