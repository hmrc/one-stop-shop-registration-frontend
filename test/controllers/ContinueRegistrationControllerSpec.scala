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

package controllers

import base.SpecBase
import connectors.SaveForLaterConnector
import forms.ContinueRegistrationFormProvider
import models.ContinueRegistration.{Continue, Delete}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.SavedProgressPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import views.html.ContinueRegistrationView

class ContinueRegistrationControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new ContinueRegistrationFormProvider()
  private val form = formProvider()

  private lazy val continueRegistrationRoute = routes.ContinueRegistrationController.onPageLoad().url

  "ContinueRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SavedProgressPage, "testUrl").success.value)).build()

      running(application) {
        val request = FakeRequest(GET, continueRegistrationRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ContinueRegistrationView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the saved url when Continue submitted" in {

      val userAnswersRepository = mock[AuthenticatedUserAnswersRepository]
      val saveForLaterConnector = mock[SaveForLaterConnector]

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SavedProgressPage, "testUrl").success.value))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(userAnswersRepository),
            bind[SaveForLaterConnector].toInstance(saveForLaterConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, continueRegistrationRoute)
            .withFormUrlEncodedBody(("value", Continue.toString))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` "testUrl"
        verifyNoInteractions(saveForLaterConnector)
        verifyNoInteractions(userAnswersRepository)
      }
    }

    "must redirect to the first page after filter questions and delete saved answers when Delete submitted" in {

      val userAnswersRepository = mock[AuthenticatedUserAnswersRepository]
      val saveForLaterConnector = mock[SaveForLaterConnector]

      when(userAnswersRepository.clear(any())) thenReturn true.toFuture
      when(saveForLaterConnector.delete()(any())) thenReturn Right(true).toFuture

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SavedProgressPage, "testUrl").success.value))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(userAnswersRepository),
            bind[SaveForLaterConnector].toInstance(saveForLaterConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, continueRegistrationRoute)
            .withFormUrlEncodedBody(("value", Delete.toString))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` controllers.auth.routes.AuthController.onSignIn().url
        verify(saveForLaterConnector, times(1)).delete()(any())
        verify(userAnswersRepository, times(1)).clear(any())
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(SavedProgressPage, "testUrl").success.value)).build()

      running(application) {
        val request =
          FakeRequest(POST, continueRegistrationRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[ContinueRegistrationView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
