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
import forms.AlreadyMadeSalesFormProvider
import models.AlreadyMadeSales.{No, Yes}
import models.{NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{AlreadyMadeSalesPage, CommencementDatePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.StartDateService
import views.html.AlreadyMadeSalesView

import scala.concurrent.Future

class AlreadyMadeSalesControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AlreadyMadeSalesFormProvider(stubClockAtArbitraryDate)
  private val form = formProvider()

  private lazy val alreadyMadeSalesRoute = routes.AlreadyMadeSalesController.onPageLoad(NormalMode).url

  private val validAnswer = No

  "AlreadyMadeSales Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alreadyMadeSalesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[AlreadyMadeSalesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(AlreadyMadeSalesPage, validAnswer).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, alreadyMadeSalesRoute)

        val view = application.injector.instanceOf[AlreadyMadeSalesView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(validAnswer), NormalMode)(request, messages(application)).toString
      }
    }

    "when the answer is No" - {

      "must save the answer and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, alreadyMadeSalesRoute)
              .withFormUrlEncodedBody(("answer", "false"))

          val result = route(application, request).value
          val expectedAnswers = emptyUserAnswers.set(AlreadyMadeSalesPage, validAnswer).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual AlreadyMadeSalesPage.navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }
    }

    "when the answer is Yes" - {

      "must save the answer and the commencement date and redirect to the next page when valid data is submitted" in {

        val mockSessionRepository = mock[SessionRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[SessionRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val request =
            FakeRequest(POST, alreadyMadeSalesRoute)
              .withFormUrlEncodedBody(
                ("answer", "true"),
                ("firstSale.day", arbitraryDate.getDayOfMonth.toString),
                ("firstSale.month", arbitraryDate.getMonthValue.toString),
                ("firstSale.year", arbitraryDate.getYear.toString)
              )

          val startDateService = application.injector.instanceOf[StartDateService]

          val result = route(application, request).value
          val expectedAnswers =
            emptyUserAnswers
              .set(AlreadyMadeSalesPage, Yes(arbitraryDate)).success.value
              .set(CommencementDatePage, startDateService.startDateBasedOnFirstSale(arbitraryDate)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual AlreadyMadeSalesPage.navigate(NormalMode, expectedAnswers).url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
        }
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, alreadyMadeSalesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[AlreadyMadeSalesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, alreadyMadeSalesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, alreadyMadeSalesRoute)
            .withFormUrlEncodedBody(("answer", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
