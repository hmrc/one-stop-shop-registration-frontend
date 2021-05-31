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
import models.euVatDetails.{Country, EuVatDetails}
import pages._
import pages.euVatDetails.VatRegisteredInEuPage
import queries.{AllEuVatDetailsQuery, AllTradingNames, AllWebsites}
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
      .set(UkVatRegisteredPostcodePage, "AA1 1AA").success.value
      .set(VatRegisteredInEuPage, true).success.value
      .set(
        AllEuVatDetailsQuery,
        List(
          EuVatDetails(Country("FR", "France"),"FR123456789", false, None, None),
          EuVatDetails(Country("ES", "Spain"),"ES123456789", false, None, None)
        )).success.value
      .set(StartDatePage,
        StartDate(StartDateOption.NextPeriod, LocalDate.now())
      ).success.value
      .set(
        BusinessAddressPage,
        Address("123 Street",Some("Street"),"City",Some("county"),"AA12 1AB")
      ).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs","01112223344","email@email.com")).success.value
      .set(AllWebsites, List("website1", "website2")).success.value

  private val registrationService = new RegistrationService()

  "fromUserAnswers" - {

    "must return a Registration when user answers are provided" in {

      val registration = registrationService.fromUserAnswers(answers, vrn)

      val expectedRegistration = RegistrationData.registration

      registration.value mustBe expectedRegistration
    }

    "must return a Registration when no trading names or EU country details were provided" in {

      val userAnswers =
        answers
          .set(HasTradingNamePage, false).success.value
          .remove(AllTradingNames).success.value
          .remove(AllEuVatDetailsQuery).success.value

      val expectedRegistration =
        RegistrationData.registration copy (
          tradingNames       = Seq.empty,
          euVatRegistrations = Seq.empty
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
