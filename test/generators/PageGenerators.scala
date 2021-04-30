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
import pages._

trait PageGenerators {

  implicit lazy val arbitraryDeleteEuVatDetailsPage: Arbitrary[DeleteEuVatDetailsPage.type] =
    Arbitrary(DeleteEuVatDetailsPage)

  implicit lazy val arbitraryAddAdditionalEuVatDetailsPage: Arbitrary[AddAdditionalEuVatDetailsPage.type] =
    Arbitrary(AddAdditionalEuVatDetailsPage)

  implicit lazy val arbitraryVatRegisteredInEuPage: Arbitrary[VatRegisteredInEuPage.type] =
    Arbitrary(VatRegisteredInEuPage)

  implicit lazy val arbitraryVatRegisteredEuMemberStatePage: Arbitrary[VatRegisteredEuMemberStatePage] =
    Arbitrary(VatRegisteredEuMemberStatePage(Index(0)))

  implicit lazy val arbitraryEuVatNumberPage: Arbitrary[EuVatNumberPage] =
    Arbitrary(EuVatNumberPage(Index(0)))

  implicit lazy val arbitraryUkVatRegisteredPostcodePage: Arbitrary[UkVatRegisteredPostcodePage.type] =
    Arbitrary(UkVatRegisteredPostcodePage)

  implicit lazy val arbitraryUkVatNumberPage: Arbitrary[UkVatNumberPage.type] =
    Arbitrary(UkVatNumberPage)

  implicit lazy val arbitraryUkVatEffectiveDatePage: Arbitrary[UkVatEffectiveDatePage.type] =
    Arbitrary(UkVatEffectiveDatePage)

  implicit lazy val arbitraryTradingNamePage: Arbitrary[TradingNamePage.type] =
    Arbitrary(TradingNamePage)

  implicit lazy val arbitraryRegisteredCompanyNamePage: Arbitrary[RegisteredCompanyNamePage.type] =
    Arbitrary(RegisteredCompanyNamePage)

  implicit lazy val arbitraryPartOfVatGroupPage: Arbitrary[PartOfVatGroupPage.type] =
    Arbitrary(PartOfVatGroupPage)

  implicit lazy val arbitraryHasTradingNamePage: Arbitrary[HasTradingNamePage.type] =
    Arbitrary(HasTradingNamePage)
}
