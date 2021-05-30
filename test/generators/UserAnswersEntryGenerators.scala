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

import models._
import models.euVatDetails.{Country, FixedEstablishmentAddress}
import org.scalacheck.Arbitrary
import org.scalacheck.Arbitrary.arbitrary
import pages._
import pages.euVatDetails.{AddEuVatDetailsPage, EuCountryPage, EuVatNumberPage, FixedEstablishmentAddressPage, FixedEstablishmentTradingNamePage, HasFixedEstablishmentPage, VatRegisteredInEuPage}
import play.api.libs.json.{JsValue, Json}

trait UserAnswersEntryGenerators extends PageGenerators with ModelGenerators {

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
        value <- arbitrary[FixedEstablishmentAddress].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryCheckVatDetailsUserAnswersEntry: Arbitrary[(CheckVatDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[CheckVatDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryStartDateUserAnswersEntry: Arbitrary[(StartDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[StartDatePage.type]
        value <- arbitrary[StartDate].map(Json.toJson(_))
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

  implicit lazy val arbitraryBusinessAddressUserAnswersEntry: Arbitrary[(BusinessAddressPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[BusinessAddressPage.type]
        value <- arbitrary[Address].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryAddAdditionalEuVatDetailsUserAnswersEntry: Arbitrary[(AddEuVatDetailsPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[AddEuVatDetailsPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryVatRegisteredInEuUserAnswersEntry: Arbitrary[(VatRegisteredInEuPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[VatRegisteredInEuPage.type]
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

  implicit lazy val arbitraryUkVatRegisteredPostcodeUserAnswersEntry: Arbitrary[(UkVatRegisteredPostcodePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UkVatRegisteredPostcodePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUkVatNumberUserAnswersEntry: Arbitrary[(UkVatNumberPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UkVatNumberPage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryUkVatEffectiveDateUserAnswersEntry: Arbitrary[(UkVatEffectiveDatePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[UkVatEffectiveDatePage.type]
        value <- arbitrary[Int].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryTradingNameUserAnswersEntry: Arbitrary[(TradingNamePage, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[TradingNamePage]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryRegisteredCompanyNameUserAnswersEntry: Arbitrary[(RegisteredCompanyNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[RegisteredCompanyNamePage.type]
        value <- arbitrary[String].suchThat(_.nonEmpty).map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryPartOfVatGroupUserAnswersEntry: Arbitrary[(PartOfVatGroupPage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[PartOfVatGroupPage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }

  implicit lazy val arbitraryHasTradingNameUserAnswersEntry: Arbitrary[(HasTradingNamePage.type, JsValue)] =
    Arbitrary {
      for {
        page  <- arbitrary[HasTradingNamePage.type]
        value <- arbitrary[Boolean].map(Json.toJson(_))
      } yield (page, value)
    }
}
