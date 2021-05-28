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

package service

import base.SpecBase
import models.{BusinessAddress, BusinessContactDetails, Country, EuVatDetails, StartDate, StartDateOption, UserAnswers}
import pages.{BusinessAddressPage, BusinessContactDetailsPage, HasTradingNamePage, PartOfVatGroupPage, RegisteredCompanyNamePage, StartDatePage, UkVatEffectiveDatePage, UkVatNumberPage, UkVatRegisteredPostcodePage, VatRegisteredInEuPage}
import queries.{AllEuVatDetailsQuery, AllTradingNames, AllWebsites}
import services.RegistrationService
import testutils.{RegistrationData, WireMockHelper}
import uk.gov.hmrc.domain.Vrn

import java.time.LocalDate

class RegistrationServiceSpec extends SpecBase with WireMockHelper {

  val answers =
    UserAnswers("id")
      .set(RegisteredCompanyNamePage, "foo").success.value
      .set(HasTradingNamePage, true).success.value
      .set(AllTradingNames, List("single", "double")).success.value
      .set(PartOfVatGroupPage, true).success.value
      .set(UkVatNumberPage, Vrn("GB123456789")).success.value
      .set(UkVatEffectiveDatePage, LocalDate.now()).success.value
      .set(UkVatRegisteredPostcodePage, "AA1 1AA").success.value
      .set(VatRegisteredInEuPage, true).success.value
      .set(
        AllEuVatDetailsQuery,
        List(EuVatDetails(Country("FR", "France"),"FR123456789"),
             EuVatDetails(Country("ES", "Spain"),"ES123456789")
        )).success.value
      .set(StartDatePage,
        StartDate(StartDateOption.NextPeriod, LocalDate.now())
      ).success.value
      .set(
        BusinessAddressPage,
        BusinessAddress("123 Street",Some("Street"),"City",Some("county"),"AA12 1AB")
      ).success.value
      .set(
        BusinessContactDetailsPage,
        BusinessContactDetails("Joe Bloggs","01112223344","email@email.com")).success.value
      .set(AllWebsites, List("website1", "website2")).success.value

  private val registrationService = new RegistrationService()

  "fromUserAnswers" - {

    "must return a Registration request when user answers are provided" in {

      val registrationRequest = registrationService.fromUserAnswers(answers)

      val request = RegistrationData.createRegistrationRequest()

      registrationRequest mustBe Some(request)
    }
  }

}
