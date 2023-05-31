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

package controllers

import base.SpecBase
import config.FrontendAppConfig
import forms.CancelAmendRegFormProvider
import models.AmendMode
import org.scalatestplus.mockito.MockitoSugar.mock
import org.mockito.Mockito.{times, verify, when}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.CancelAmendRegistrationView
import play.api.inject.bind
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.Future

class CancelAmendRegistrationControllerSpec extends SpecBase {

  private val formProvider = new CancelAmendRegFormProvider
  private val form = formProvider()

  private lazy val CancelAmendRoute = routes.CancelAmendRegistrationController.onPageLoad().url

  "CancelAmendRegistration Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, CancelAmendRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CancelAmendRegistrationView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, AmendMode)(request, messages(application)).toString
      }
    }

    "must delete the amended answers and return OK and the yourAccount view for a POST" in {
      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val sessionId = "12345-credId"
        val request =
          FakeRequest(POST, CancelAmendRoute)
            .withSession(SessionKeys.sessionId -> sessionId)

        val config = application.injector.instanceOf[FrontendAppConfig]

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual config.ossYourAccountUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(sessionId))
      }
    }
  }
}
