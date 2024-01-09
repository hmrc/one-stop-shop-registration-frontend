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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import forms.CancelAmendRegFormProvider
import models.AmendMode
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.Mockito.{never, times, verify, when}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.CancelAmendRegistrationView
import play.api.inject.bind
import testutils.RegistrationData
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

class CancelAmendRegistrationControllerSpec extends SpecBase {

  private val formProvider = new CancelAmendRegFormProvider
  private val form = formProvider()
  private lazy val CancelAmendRoute = routes.CancelAmendRegistrationController.onPageLoad().url

  private val mockRegistrationConnector = mock[RegistrationConnector]

  "CancelAmendRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .build()

      running(application) {
        val request = FakeRequest(GET, CancelAmendRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelAmendRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, AmendMode)(request, messages(application)).toString
      }
    }

    "must delete the amended answers and redirect to yourAccount page when the user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)


      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()

      running(application) {
        val sessionId = "12345-credId"
        val request =
          FakeRequest(POST, CancelAmendRoute)
            .withSession(SessionKeys.sessionId -> sessionId)
            .withFormUrlEncodedBody(("value", "true"))

        val config = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual config.ossYourAccountUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(sessionId))
      }
    }

    "must NOT delete the amended answers and returns user to ChangeYourRegistration page when the user answers No" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)


      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .build()

      running(application) {
        val sessionId = "12345-credId"
        val request =
          FakeRequest(POST, CancelAmendRoute)
            .withSession(SessionKeys.sessionId -> sessionId)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.amend.routes.ChangeYourRegistrationController.onPageLoad().url
        verify(mockSessionRepository, never()).set(eqTo(basicUserAnswersWithVatInfo))
      }
    }

  }
}
