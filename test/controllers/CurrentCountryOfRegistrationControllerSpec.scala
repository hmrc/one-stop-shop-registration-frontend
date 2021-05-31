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

package controllers

import base.SpecBase
import forms.CurrentCountryOfRegistrationFormProvider
import models.euVatDetails.Country
import models.{Index, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.CurrentCountryOfRegistrationPage
import pages.euVatDetails.{EuCountryPage, EuVatNumberPage, HasFixedEstablishmentPage}
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.CurrentCountryOfRegistrationViewModel
import views.html.CurrentCountryOfRegistrationView

import scala.concurrent.Future

class CurrentCountryOfRegistrationControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private lazy val currentCountryOfRegistrationRoute = routes.CurrentCountryOfRegistrationController.onPageLoad(NormalMode).url

  private val formProvider = new CurrentCountryOfRegistrationFormProvider()
  private val countries    = Seq(Country.euCountries.head)

  private val baseAnswers =
    emptyUserAnswers
      .set(EuCountryPage(Index(0)), countries.head).success.value
      .set(EuVatNumberPage(Index(0)), "foo").success.value
      .set(HasFixedEstablishmentPage(Index(0)), false).success.value

  private val viewModel = new CurrentCountryOfRegistrationViewModel(countries)
  private val form      = formProvider(countries)

  "CurrentCountryOfRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, currentCountryOfRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CurrentCountryOfRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(CurrentCountryOfRegistrationPage, countries.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, currentCountryOfRegistrationRoute)

        val view = application.injector.instanceOf[CurrentCountryOfRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(countries.head), NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, currentCountryOfRegistrationRoute)
            .withFormUrlEncodedBody(("value", countries.head.code))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, currentCountryOfRegistrationRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CurrentCountryOfRegistrationView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, viewModel)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no EU countries have been entered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, currentCountryOfRegistrationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no EU countries have been entered" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, currentCountryOfRegistrationRoute)
            .withFormUrlEncodedBody(("value", countries.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, currentCountryOfRegistrationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, currentCountryOfRegistrationRoute)
            .withFormUrlEncodedBody(("value", countries.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
