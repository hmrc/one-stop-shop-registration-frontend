/*
 * Copyright 2024 HM Revenue & Customs
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

package utils

import base.SpecBase
import models.euDetails.EuOptionalDetails
import models.{BankDetails, BusinessContactDetails, Country, Iban, PreviousScheme}
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.PreviouslyRegisteredPage
import pages.{BankDetailsPage, BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage, HasTradingNamePage, HasWebsitePage, IsOnlineMarketplacePage}
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import queries.previousRegistration.AllPreviousRegistrationsWithOptionalVatNumberQuery
import queries.{AllEuOptionalDetailsQuery, AllTradingNames, AllWebsites}

class CompletionChecksSpec extends SpecBase with MockitoSugar {

  object TestCompletionChecks extends CompletionChecks

  private val completeAnswers = completeUserAnswers
    .set(HasTradingNamePage, true).success.value
    .set(AllTradingNames, List("Trading Name")).success.value
    .set(HasMadeSalesPage, true).success.value
    .set(DateOfFirstSalePage, arbitraryDate).success.value
    .set(IsOnlineMarketplacePage, false).success.value
    .set(HasWebsitePage, true).success.value
    .set(AllWebsites, List("website.com")).success.value
    .set(PreviouslyRegisteredPage, false).success.value
    .set(BusinessContactDetailsPage, BusinessContactDetails("fullname", "123456789", "unittest@email.com")).success.value
    .set(BankDetailsPage, BankDetails("unit test account name", None, Iban("GB33BUKB20201555555555").value)).success.value

  "CompletionChecks" - {

    "getAllIncompleteDeregisteredDetails" - {

      "should return incomplete details" in {

        val userAnswers = completeUserAnswers.set(AllPreviousRegistrationsWithOptionalVatNumberQuery, List(
          PreviousRegistrationDetailsWithOptionalVatNumber(
            Country("DE", "Germany"),
            Some(List(SchemeDetailsWithOptionalVatNumber(Some(PreviousScheme.OSSU), None))))
        )).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(userAnswers)

          val result = TestCompletionChecks.getAllIncompleteDeregisteredDetails()
          result must have length 1
          result.head.previousEuCountry mustEqual Country("DE", "Germany")
        }

      }
    }

    "firstIndexedIncompleteDeregisteredCountry" - {

      "should return first incomplete country" in {

        val userAnswers = completeUserAnswers.set(AllPreviousRegistrationsWithOptionalVatNumberQuery, List(
          PreviousRegistrationDetailsWithOptionalVatNumber(
            Country("DE", "Germany"),
            Some(List(SchemeDetailsWithOptionalVatNumber(Some(PreviousScheme.OSSU), None)))),
          PreviousRegistrationDetailsWithOptionalVatNumber(
            Country("FR", "France"),
            Some(List(SchemeDetailsWithOptionalVatNumber(Some(PreviousScheme.OSSU), None))))
        )).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(userAnswers)

          val result = TestCompletionChecks.firstIndexedIncompleteDeregisteredCountry(Seq(Country("DE", "Germany")))
          result mustBe defined
          result.get._1.previousEuCountry mustEqual Country("DE", "Germany")

        }
      }
    }

    "firstIndexedIncompleteEuDetails" - {

      "should return the first incomplete EuOptionalDetails" in {

        val incompleteDetails1 = EuOptionalDetails(Country("BE", "Belgium"), Some(true), None, None, None, None, None, None, None, None, None)
        val incompleteDetails2 = EuOptionalDetails(Country("FR", "France"), Some(true), None, None, None, None, None, None, None, None, None)
        val allDetails = List(incompleteDetails1, incompleteDetails2)
        val answers = completeUserAnswers.set(AllEuOptionalDetailsQuery, allDetails).success.value

        val application = applicationBuilder(userAnswers = Some(answers)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(
            FakeRequest(), testCredentials, vrn, None, answers
          )

          val result = TestCompletionChecks.firstIndexedIncompleteEuDetails(Seq(Country("BE", "Belgium")))

          result mustBe Some((incompleteDetails1, 0))
        }
      }
    }

    "validate" - {
      "should return true if all validations pass" in {

        val application = applicationBuilder(userAnswers = Some(completeAnswers)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(completeAnswers)

          TestCompletionChecks.validate() mustBe true
        }
      }

      "should return false if a validation fails" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(basicUserAnswersWithVatInfo)

          TestCompletionChecks.validate() mustBe false
        }
      }

      "should return false if contact details are missing" in {

        val answersWithoutContactDetails = completeAnswers.remove(BusinessContactDetailsPage).success.value

        val application = applicationBuilder(userAnswers = Some(answersWithoutContactDetails)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(answersWithoutContactDetails)

          TestCompletionChecks.validate() mustBe false
        }
      }

      "should return false if bank details are missing" in {

        val answersWithoutBankDetails = completeAnswers.remove(BankDetailsPage).success.value

        val application = applicationBuilder(userAnswers = Some(answersWithoutBankDetails)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(answersWithoutBankDetails)

          TestCompletionChecks.validate() mustBe false
        }
      }

      "should return false if online marketplace question is missing" in {

        val answersWithoutIsOnlineMarketplace = completeAnswers.remove(IsOnlineMarketplacePage).success.value

        val application = applicationBuilder(userAnswers = Some(answersWithoutIsOnlineMarketplace)).build()

        running(application) {
          implicit val request: AuthenticatedDataRequest[AnyContent] = mock[AuthenticatedDataRequest[AnyContent]]
          when(request.userAnswers).thenReturn(answersWithoutIsOnlineMarketplace)

          TestCompletionChecks.validate() mustBe false
        }
      }
    }


  }

}
