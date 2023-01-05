/*
 * Copyright 2023 HM Revenue & Customs
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

import models._
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.euDetails._
import pages.previousRegistrations._
import play.api.libs.json.{Json, JsValue}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

  implicit lazy val arbitraryPreviousIossNumberUserAnswersEntry: Arbitrary[(PreviousSchemeNumbersPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousSchemeNumbersPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousIossSchemeUserAnswersEntry: Arbitrary[(PreviousIossSchemePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousIossSchemePage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousSchemePageUserAnswersEntry: Arbitrary[(PreviousSchemePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousSchemePage]
        value <- arbitrary[PreviousScheme].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousSchemeTypePageUserAnswersEntry: Arbitrary[(PreviousSchemeTypePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousSchemeTypePage]
        value <- arbitrary[PreviousSchemeType].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuSendGoodsUserAnswersEntry: Arbitrary[(EuSendGoodsPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuSendGoodsPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuSendGoodsTradingNameUserAnswersEntry: Arbitrary[(EuSendGoodsTradingNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuSendGoodsTradingNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuSendGoodsAddressUserAnswersEntry: Arbitrary[(EuSendGoodsAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuSendGoodsAddressPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsPlanningFirstEligibleSaleUserAnswersEntry: Arbitrary[(IsPlanningFirstEligibleSalePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsPlanningFirstEligibleSalePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitrarySalesChannelsUserAnswersEntry: Arbitrary[(SalesChannelsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[SalesChannelsPage.type]
        value <- arbitrary[SalesChannels].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasFixedEstablishmentInNiUserAnswersEntry: Arbitrary[(HasFixedEstablishmentInNiPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasFixedEstablishmentInNiPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessBasedInNiUserAnswersEntry: Arbitrary[(BusinessBasedInNiPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessBasedInNiPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryIsOnlineMarketplaceUserAnswersEntry: Arbitrary[(IsOnlineMarketplacePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[IsOnlineMarketplacePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryDateOfFirstSaleUserAnswersEntry: Arbitrary[(DateOfFirstSalePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[DateOfFirstSalePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTaxRegisteredInEuUserAnswersEntry: Arbitrary[(TaxRegisteredInEuPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TaxRegisteredInEuPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasWebsiteUserAnswersEntry: Arbitrary[(HasWebsitePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasWebsitePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuTaxReferenceUserAnswersEntry: Arbitrary[(EuTaxReferencePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuTaxReferencePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBankDetailsUserAnswersEntry: Arbitrary[(BankDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BankDetailsPage.type]
        value <- arbitrary[BankDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviouslyRegisteredUserAnswersEntry: Arbitrary[(PreviouslyRegisteredPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviouslyRegisteredPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousEuVatNumberUserAnswersEntry: Arbitrary[(PreviousOssNumberPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousOssNumberPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPreviousEuCountryUserAnswersEntry: Arbitrary[(PreviousEuCountryPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PreviousEuCountryPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddPreviousRegistrationUserAnswersEntry: Arbitrary[(AddPreviousRegistrationPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddPreviousRegistrationPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasFixedEstablishmentUserAnswersEntry: Arbitrary[(HasFixedEstablishmentPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasFixedEstablishmentPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryFixedEstablishmentTradingNameUserAnswersEntry: Arbitrary[(FixedEstablishmentTradingNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[FixedEstablishmentTradingNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryFixedEstablishmentAddressUserAnswersEntry: Arbitrary[(FixedEstablishmentAddressPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[FixedEstablishmentAddressPage]
        value <- arbitrary[InternationalAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCheckVatDetailsUserAnswersEntry: Arbitrary[(CheckVatDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CheckVatDetailsPage.type]
        value <- arbitrary[CheckVatDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryWebsiteUserAnswersEntry: Arbitrary[(WebsitePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[WebsitePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryBusinessContactDetailsUserAnswersEntry: Arbitrary[(BusinessContactDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessContactDetailsPage.type]
        value <- arbitrary[BusinessContactDetails].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddAdditionalEuVatDetailsUserAnswersEntry: Arbitrary[(AddEuDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddEuDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVatRegisteredInEuUserAnswersEntry: Arbitrary[(VatRegisteredPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VatRegisteredPage]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuCountry: Arbitrary[(EuCountryPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuCountryPage]
        value <- arbitrary[Country].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryEuVatNumberUserAnswersEntry: Arbitrary[(EuVatNumberPage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[EuVatNumberPage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTradingNameUserAnswersEntry: Arbitrary[(TradingNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TradingNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

}
