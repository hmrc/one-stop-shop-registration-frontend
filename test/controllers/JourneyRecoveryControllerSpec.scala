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
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.http.SessionKeys
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryMissingUserAnswersStartAgainView, JourneyRecoveryStartAgainView}

import scala.concurrent.Future

class JourneyRecoveryControllerSpec extends SpecBase {

  "JourneyRecovery Controller" - {

    ".onPageLoad" - {

      "when a relative continue Url is supplied" - {

        "must return OK and the continue view" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val continueUrl = RedirectUrl("/foo")
            val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(Some(continueUrl)).url)

            val result = route(application, request).value

            val continueView = application.injector.instanceOf[JourneyRecoveryContinueView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual continueView(continueUrl.unsafeValue)(request, messages(application)).toString
          }
        }
      }

      "when an absolute continue Url is supplied" - {

        "must return OK and the start again view" in {

          val application = applicationBuilder(userAnswers = None).build()

          running(application) {
            val continueUrl = RedirectUrl("https://foo.com")
            val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad(Some(continueUrl)).url)

            val result = route(application, request).value

            val startAgainView = application.injector.instanceOf[JourneyRecoveryStartAgainView]

            status(result) mustEqual OK
            contentAsString(result) mustEqual startAgainView()(request, messages(application)).toString
          }
        }
      }

      "when no continue Url is supplied" - {

        "must return OK and the start again view with link to auth on sign in" in {

          val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

          running(application) {
            val request = FakeRequest(GET, routes.JourneyRecoveryController.onPageLoad().url)

            val result = route(application, request).value

            val startAgainView = application.injector.instanceOf[JourneyRecoveryStartAgainView]

            val expectedRedirectURl = controllers.auth.routes.AuthController.onSignIn().url

            status(result) mustEqual OK
            contentAsString(result) mustEqual startAgainView(expectedRedirectURl)(request, messages(application)).toString
          }
        }
      }
    }

    ".onMissingAnswers" - {

      "must delete all user answers and return OK and the correct view for a GET when there are empty user answers" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val sessionId = userAnswersId
          val request = FakeRequest(GET, routes.JourneyRecoveryController.onMissingAnswers().url)
            .withSession(SessionKeys.sessionId -> sessionId)


          val result = route(application, request).value

          val missingAnswersView = application.injector.instanceOf[JourneyRecoveryMissingUserAnswersStartAgainView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual missingAnswersView()(request, messages(application)).toString
          verify(mockSessionRepository, times(1)).clear(eqTo(sessionId))

        }
      }
    }
  }
}
