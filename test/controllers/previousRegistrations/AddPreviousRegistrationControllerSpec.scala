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

package controllers.previousRegistrations

import base.SpecBase
import forms.previousRegistrations.AddPreviousRegistrationFormProvider
import models.{Country, Index, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.previousRegistrations.{AddPreviousRegistrationPage, PreviousEuCountryPage, PreviousEuVatNumberPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.checkAnswers.previousRegistrations.PreviousRegistrationSummary
import views.html.previousRegistrations.AddPreviousRegistrationView

import scala.concurrent.Future

class AddPreviousRegistrationControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new AddPreviousRegistrationFormProvider()
  private val form = formProvider()

  private lazy val addPreviousRegistrationRoute = routes.AddPreviousRegistrationController.onPageLoad(NormalMode).url

  private val baseAnswers =
    emptyUserAnswers
      .set(PreviousEuCountryPage(Index(0)), Country.euCountries.head).success.value
      .set(PreviousEuVatNumberPage(Index(0)), "foo").success.value

  "AddPreviousRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list                    = PreviousRegistrationSummary.addToListRows(baseAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
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
          FakeRequest(POST, addPreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AddPreviousRegistrationView]
        implicit val msgs: Messages = messages(application)
        val list                    = PreviousRegistrationSummary.addToListRows(baseAnswers)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addPreviousRegistrationRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addPreviousRegistrationRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}