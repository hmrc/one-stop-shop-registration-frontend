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

package controllers.euDetails

import base.SpecBase
import controllers.euDetails.{routes => euDetailsRoutes}
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, SellsGoodsToEUConsumersPage, TaxRegisteredInEuPage, VatRegisteredPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EuDetailsQuery
import repositories.AuthenticatedUserAnswersRepository
import views.html.euDetails.CannotAddCountryWithoutVatNumberView

import scala.concurrent.Future

class CannotAddCountryWithoutVatNumberControllerSpec extends SpecBase with MockitoSugar {

  private val countryIndex: Index = Index(0)
  private val countryIndex1: Index = Index(1)
  private val country = Country.euCountries.head

  private val answers =
    basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatCustomerInfo.copy(partOfVatGroup = true)))
      .set(TaxRegisteredInEuPage, true).success.value
      .set(EuCountryPage(countryIndex), country).success.value
      .set(SellsGoodsToEUConsumersPage(countryIndex), false).success.value
      .set(VatRegisteredPage(countryIndex), false).success.value

  private lazy val cannotAddCountryWithoutVatNumberRoute = euDetailsRoutes.CannotAddCountryWithoutVatNumberController.onPageLoad(NormalMode, countryIndex).url

  "CannotAddCountryWithoutVatNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, cannotAddCountryWithoutVatNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotAddCountryWithoutVatNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must delete a record and redirect to next page when submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cannotAddCountryWithoutVatNumberRoute)

        val result = route(application, request).value
        val expectedAnswers = answers.remove(EuDetailsQuery(countryIndex)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(NormalMode).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must delete a record and redirect to the next page when the user submits and other records exist" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val additionalAnswers = answers
        .set(TaxRegisteredInEuPage, true).success.value
        .set(EuCountryPage(countryIndex1), country).success.value
        .set(SellsGoodsToEUConsumersPage(countryIndex1), false).success.value
        .set(VatRegisteredPage(countryIndex1), false).success.value

      val application =
        applicationBuilder(userAnswers = Some(additionalAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, cannotAddCountryWithoutVatNumberRoute)

        val result = route(application, request).value
        val expectedAnswers = additionalAnswers.remove(EuDetailsQuery(countryIndex)).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.euDetails.routes.AddEuDetailsController.onPageLoad(NormalMode).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }
  }
}
