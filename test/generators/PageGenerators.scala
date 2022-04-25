/*
 * Copyright 2022 HM Revenue & Customs
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
import pages.{euDetails, _}
import pages.euDetails.{AddEuDetailsPage, EuCountryPage, EuSendGoodsTradingNamePage, EuTaxReferencePage, EuVatNumberPage, FixedEstablishmentAddressPage, FixedEstablishmentTradingNamePage, HasFixedEstablishmentPage, TaxRegisteredInEuPage, VatRegisteredPage}
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}

trait PageGenerators {

  implicit lazy val arbitraryEuSendGoodsTradingNamePage: Arbitrary[EuSendGoodsTradingNamePage] =
    Arbitrary(EuSendGoodsTradingNamePage(Index(0)))

  implicit lazy val arbitraryIsPlanningFirstEligibleSalePage: Arbitrary[IsPlanningFirstEligibleSalePage.type] =
    Arbitrary(IsPlanningFirstEligibleSalePage)

  implicit lazy val arbitrarySalesChannelsPage: Arbitrary[SalesChannelsPage.type] =
    Arbitrary(SalesChannelsPage)

  implicit lazy val arbitraryHasFixedEstablishmentInNiPage: Arbitrary[HasFixedEstablishmentInNiPage.type] =
    Arbitrary(HasFixedEstablishmentInNiPage)

  implicit lazy val arbitraryBusinessBasedInNiPage: Arbitrary[BusinessBasedInNiPage.type] =
    Arbitrary(BusinessBasedInNiPage)

  implicit lazy val arbitraryIsOnlineMarketplacePage: Arbitrary[IsOnlineMarketplacePage.type] =
    Arbitrary(IsOnlineMarketplacePage)

  implicit lazy val arbitraryHasMadeSalesPage: Arbitrary[HasMadeSalesPage.type] =
    Arbitrary(HasMadeSalesPage)

  implicit lazy val arbitraryDateOfFirstSalePage: Arbitrary[DateOfFirstSalePage.type] =
    Arbitrary(DateOfFirstSalePage)

  implicit lazy val arbitraryTaxRegisteredInEuPage: Arbitrary[TaxRegisteredInEuPage.type] =
    Arbitrary(TaxRegisteredInEuPage)

  implicit lazy val arbitrarySellsGoodsFromNiPage: Arbitrary[SellsGoodsFromNiPage.type] =
    Arbitrary(SellsGoodsFromNiPage)

  implicit lazy val arbitraryHasWebsitePage: Arbitrary[HasWebsitePage.type] =
    Arbitrary(HasWebsitePage)

  implicit lazy val arbitraryEuTaxReferencePage: Arbitrary[EuTaxReferencePage] =
    Arbitrary(EuTaxReferencePage(Index(0)))

  implicit lazy val arbitraryBankDetailsPage: Arbitrary[BankDetailsPage.type] =
    Arbitrary(BankDetailsPage)

  implicit lazy val arbitraryPreviouslyRegisteredPage: Arbitrary[PreviouslyRegisteredPage.type] =
    Arbitrary(PreviouslyRegisteredPage)

  implicit lazy val arbitraryPreviousEuVatNumberPage: Arbitrary[PreviousEuVatNumberPage] =
    Arbitrary(PreviousEuVatNumberPage(Index(0)))

  implicit lazy val arbitraryPreviousEuCountryPage: Arbitrary[PreviousEuCountryPage] =
    Arbitrary(PreviousEuCountryPage(Index(0)))

  implicit lazy val arbitraryAddPreviousRegistrationPage: Arbitrary[AddPreviousRegistrationPage.type] =
    Arbitrary(AddPreviousRegistrationPage)

  implicit lazy val arbitraryHasFixedEstablishmentPage: Arbitrary[HasFixedEstablishmentPage] =
    Arbitrary(euDetails.HasFixedEstablishmentPage(Index(0)))

  implicit lazy val arbitraryFixedEstablishmentTradingNamePage: Arbitrary[FixedEstablishmentTradingNamePage] =
    Arbitrary(euDetails.FixedEstablishmentTradingNamePage(Index(0)))

  implicit lazy val arbitraryFixedEstablishmentAddressPage: Arbitrary[FixedEstablishmentAddressPage] =
    Arbitrary(euDetails.FixedEstablishmentAddressPage(Index(0)))

  implicit lazy val arbitraryCheckVatDetailsPage: Arbitrary[CheckVatDetailsPage.type] =
    Arbitrary(CheckVatDetailsPage)

  implicit lazy val arbitraryWebsitePage: Arbitrary[WebsitePage] =
    Arbitrary(WebsitePage(Index(0)))

  implicit lazy val arbitraryBusinessContactDetailsPage: Arbitrary[BusinessContactDetailsPage.type] =
    Arbitrary(BusinessContactDetailsPage)

  implicit lazy val arbitraryAddEuVatDetailsPage: Arbitrary[AddEuDetailsPage.type] =
    Arbitrary(AddEuDetailsPage)

  implicit lazy val arbitraryVatRegisteredPage: Arbitrary[VatRegisteredPage] =
    Arbitrary(VatRegisteredPage(Index(0)))

  implicit lazy val arbitraryEuCountryPage: Arbitrary[EuCountryPage] =
    Arbitrary(euDetails.EuCountryPage(Index(0)))

  implicit lazy val arbitraryEuVatNumberPage: Arbitrary[EuVatNumberPage] =
    Arbitrary(euDetails.EuVatNumberPage(Index(0)))

  implicit lazy val arbitraryTradingNamePage: Arbitrary[TradingNamePage] =
    Arbitrary(TradingNamePage(Index(0)))

}
