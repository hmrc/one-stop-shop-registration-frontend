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

package controllers.previousRegistrations

import base.SpecBase
import forms.previousRegistrations.PreviousEuVatNumberFormProvider
import models.{Country, CountryWithValidationDetails, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousEuVatNumberPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.previousRegistrations.PreviousEuVatNumberView

import scala.concurrent.Future

class PreviousEuVatNumberControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)
  private val country = Country("SI", "Slovenia")
  private val countryWithValidation = CountryWithValidationDetails.euCountriesWithVRNValidationRules.find(_.country.code == "SI").value
  private val formProvider = new PreviousEuVatNumberFormProvider()
  private val form = formProvider(country)

  private lazy val previousEuVatNumberRoute = routes.PreviousEuVatNumberController.onPageLoad(NormalMode, index).url

  private val baseAnswers = basicUserAnswers.set(PreviousEuCountryPage(index), country).success.value


  "PreviousEuVatNumber Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousEuVatNumberRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreviousEuVatNumberView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(PreviousEuVatNumberPage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousEuVatNumberRoute)

        val view = application.injector.instanceOf[PreviousEuVatNumberView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuVatNumberRoute)
            .withFormUrlEncodedBody(("value", "12345678"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(PreviousEuVatNumberPage(index), "12345678").success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PreviousEuVatNumberPage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuVatNumberRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PreviousEuVatNumberView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, countryWithValidation)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousEuVatNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousEuVatNumberRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuVatNumberRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
