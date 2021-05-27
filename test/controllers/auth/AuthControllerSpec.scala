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

package controllers.auth

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.auth.{InsufficientEnrolmentsView, UnsupportedAffinityGroupView, UnsupportedAuthProviderView, UnsupportedCredentialRoleView}

import java.net.URLEncoder
import scala.concurrent.Future

class AuthControllerSpec extends SpecBase with MockitoSugar {

  private val continueUrl = "continueUrl"

  "signOut" - {

    "must clear user answers and redirect to sign out, specifying the exit survey as the continue URL" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(None)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthController.signOut().url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(appConfig.exitSurveyUrl, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(userAnswersId))
      }
    }
  }

  "signOutNoSurvey" - {

    "must clear users answers and redirect to sign out, specifying SignedOut as the continue URL" in {

      val mockSessionRepository = mock[SessionRepository]
      when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(None)
          .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {

        val appConfig = application.injector.instanceOf[FrontendAppConfig]
        val request   = FakeRequest(GET, routes.AuthController.signOutNoSurvey().url)

        val result = route(application, request).value

        val encodedContinueUrl  = URLEncoder.encode(routes.SignedOutController.onPageLoad().url, "UTF-8")
        val expectedRedirectUrl = s"${appConfig.signOutUrl}?continue=$encodedContinueUrl"

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual expectedRedirectUrl
        verify(mockSessionRepository, times(1)).clear(eqTo(userAnswersId))
      }
    }
  }

  "redirectToRegister" - {

    "must redirect the user to bas-gateway to register" in {

      val application = applicationBuilder(Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToRegister(continueUrl).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/register?origin=OSS&continueUrl=continueUrl&accountType=Organisation"
      }
    }
  }

  "redirectToLogin" - {

    "must redirect the user to bas-gateway to log in" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.redirectToLogin(continueUrl).url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual "http://localhost:9553/bas-gateway/sign-in?origin=OSS&continue=continueUrl"
      }
    }
  }

  "unsupportedAuthProvider" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAuthProvider(continueUrl).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedAuthProviderView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view(continueUrl)(request, messages(application)).toString
      }
    }
  }

  "unsupportedAffinityGroup" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedAffinityGroup().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedAffinityGroupView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }

  "unsupportedCredentialRole" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.unsupportedCredentialRole().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[UnsupportedCredentialRoleView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }


  "insufficientEnrolments" - {

    "must return OK and the correct view" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.AuthController.insufficientEnrolments().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[InsufficientEnrolmentsView]

        status(result) mustEqual OK

        contentAsString(result) mustEqual
          view()(request, messages(application)).toString
      }
    }
  }
}
