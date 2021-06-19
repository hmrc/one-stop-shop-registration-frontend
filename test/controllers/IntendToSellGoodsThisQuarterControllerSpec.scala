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
import forms.IntendToSellGoodsThisQuarterFormProvider
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CommencementDatePage, IntendToSellGoodsThisQuarterPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import views.html.IntendToSellGoodsThisQuarterView

import java.time.LocalDate
import scala.concurrent.Future

class IntendToSellGoodsThisQuarterControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new IntendToSellGoodsThisQuarterFormProvider()
  private val form = formProvider()

  private lazy val intendToSellGoodsThisQuarterRoute = routes.IntendToSellGoodsThisQuarterController.onPageLoad(NormalMode).url

  "IntendToSellGoodsThisQuarter Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, intendToSellGoodsThisQuarterRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IntendToSellGoodsThisQuarterView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(IntendToSellGoodsThisQuarterPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, intendToSellGoodsThisQuarterRoute)

        val view = application.injector.instanceOf[IntendToSellGoodsThisQuarterView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode)(request, messages(application)).toString
      }
    }

    "when the answer is yes" - {

      "must save the answer and the commencement date and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, intendToSellGoodsThisQuarterRoute)
              .withFormUrlEncodedBody(("value", "true"))

          val result = route(application, request).value
          val expectedAnswers =
            emptyUserAnswers
              .set(IntendToSellGoodsThisQuarterPage, true).success.value
              .set(CommencementDatePage, LocalDate.now(stubClockAtArbitraryDate)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual IntendToSellGoodsThisQuarterPage.navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }
    }

    "when the answer is no" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, intendToSellGoodsThisQuarterRoute)
              .withFormUrlEncodedBody(("value", "false"))

          val result = route(application, request).value
          val expectedAnswers = emptyUserAnswers.set(IntendToSellGoodsThisQuarterPage, false).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual IntendToSellGoodsThisQuarterPage.navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, intendToSellGoodsThisQuarterRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IntendToSellGoodsThisQuarterView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, intendToSellGoodsThisQuarterRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, intendToSellGoodsThisQuarterRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
