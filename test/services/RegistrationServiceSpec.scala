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

package services

import base.SpecBase
import models._
import models.domain.VatDetailSource.UserEntered
import models.domain.{VatCustomerInfo, VatDetailSource, VatDetails}
import models.euDetails.EuDetails
import pages._
import pages.euDetails.{TaxRegisteredInEuPage}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousEuVatNumberPage, PreviouslyRegisteredPage}
import queries.{AllEuDetailsQuery, AllTradingNames, AllWebsites}
import testutils.RegistrationData

import java.time.LocalDate

class RegistrationServiceSpec extends SpecBase {

  private val answers =
    UserAnswers("id")
      .set(RegisteredCompanyNamePage, "foo").success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(PartOfVatGroupPage, true).success.value
      .set(UkVatEffectiveDatePage, LocalDate.now()).success.value
      .set(TaxRegisteredInEuPage, true).success.value
      .set(
        AllEuDetailsQuery,
        List(
          EuDetails(Country("FR", "France"),"FR123456789", false, None, None),
          EuDetails(Country("ES", "Spain"),"ES123456789", false, None, None)
        )).success.value
      .set(StartDatePage,
        StartDate(StartDateOption.NextPeriod, LocalDate.now())
      ).success.value
      .set(
        BusinessAddressPage,
        UkAddress("123 Street",Some("Street"),"City",Some("county"),"AA12 1AB")
      ).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs","01112223344","email@email.com")).success.value
      .set(AllWebsites, List("website1", "website2")).success.value
      .set(CurrentlyRegisteredInEuPage, true).success.value
      .set(CurrentCountryOfRegistrationPage, Country("FR", "France")).success.value
      .set(PreviouslyRegisteredPage, true).success.value
      .set(PreviousEuCountryPage(Index(0)), Country("DE", "Germany")).success.value
      .set(PreviousEuVatNumberPage(Index(0)), "DE123").success.value

  private val registrationService = new RegistrationService()

  "fromUserAnswers" - {

    "must return a Registration when user answers are provided and the user manually entered all their VAT details" in {

      val registration = registrationService.fromUserAnswers(answers, vrn)

      val expectedRegistration = RegistrationData.registration copy (vatDetails = RegistrationData.registration.vatDetails.copy (source = UserEntered))

      registration.value mustBe expectedRegistration
    }

    "must return a Registration when user answers are provided and we have full VAT information on the user" in {

      val regDate = LocalDate.of(2000, 1, 1)
      val address = DesAddress("Line 1", None, None, None, None, Some("BB22 2BB"), "GB")
      val vatInfo = VatCustomerInfo(
        registrationDate = Some(regDate),
        address          = address,
        partOfVatGroup   = Some(true),
        organisationName = Some("bar")
      )

      val userAnswers =
        answers.copy(vatInfo = Some(vatInfo))
          .remove(UkVatEffectiveDatePage).success.value
          .remove(BusinessAddressPage).success.value
          .remove(PartOfVatGroupPage).success.value

      val registration = registrationService.fromUserAnswers(userAnswers, vrn)

      val expectedRegistration =
        RegistrationData.registration.copy(
          vatDetails = VatDetails(regDate, address, true, VatDetailSource.Etmp),
          registeredCompanyName = "bar"
        )

      registration.value mustEqual expectedRegistration
    }

    "must return a Registration when no trading names or EU country details were provided" in {

      val userAnswers =
        answers
          .set(HasTradingNamePage, false).success.value
          .remove(AllTradingNames).success.value
          .remove(AllEuDetailsQuery).success.value

      val expectedRegistration =
        RegistrationData.registration copy (
          tradingNames       = Seq.empty,
          euVatRegistrations = Seq.empty,
          vatDetails         = RegistrationData.registration.vatDetails copy (source = UserEntered)
        )

      val registration = registrationService.fromUserAnswers(userAnswers, vrn)
      registration.value mustEqual expectedRegistration
    }

    "must return None when mandatory data is missing" in {

      val userAnswers = answers.remove(RegisteredCompanyNamePage).success.value
      val result = registrationService.fromUserAnswers(userAnswers, vrn)

      result mustBe empty
    }
  }
}
