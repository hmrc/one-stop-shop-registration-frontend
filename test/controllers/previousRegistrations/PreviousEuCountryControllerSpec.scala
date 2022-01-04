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
import forms.previousRegistrations.PreviousEuCountryFormProvider
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.PreviousEuCountryPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedSessionRepository
import views.html.previousRegistrations.PreviousEuCountryView

import scala.concurrent.Future

class PreviousEuCountryControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)
  private val formProvider = new PreviousEuCountryFormProvider()
  private val form = formProvider(index, Seq.empty)

  private lazy val previousEuCountryRoute = routes.PreviousEuCountryController.onPageLoad(NormalMode, index).url

  private val country = Country.euCountries.head

  "PreviousEuCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousEuCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[PreviousEuCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = emptyUserAnswers.set(PreviousEuCountryPage(index), country).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, previousEuCountryRoute)

        val view = application.injector.instanceOf[PreviousEuCountryView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(country), NormalMode, index)(request, messages(application)).toString
      }
    }

    "must save the answer andredirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedSessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[AuthenticatedSessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuCountryRoute)
            .withFormUrlEncodedBody(("value", country.code))

        val result = route(application, request).value
        val expectedAnswer = emptyUserAnswers.set(PreviousEuCountryPage(index), country).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual PreviousEuCountryPage(index).navigate(NormalMode, expectedAnswer).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswer))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[PreviousEuCountryView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, previousEuCountryRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, previousEuCountryRoute)
            .withFormUrlEncodedBody(("value", country.code))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
