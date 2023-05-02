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
import forms.DeleteAllTradingNamesFormProvider
import models.{AmendMode, CheckMode, Index}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{DeleteAllTradingNamesPage, HasTradingNamePage, TradingNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.AllTradingNames
import repositories.AuthenticatedUserAnswersRepository
import views.html.DeleteAllTradingNamesView

import scala.concurrent.Future

class DeleteAllTradingNamesControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DeleteAllTradingNamesFormProvider()
  private val form = formProvider()

  private val userAnswers = basicUserAnswersWithVatInfo
    .set(TradingNamePage(Index(0)), "foo trading name").success.value
    .set(TradingNamePage(Index(1)), "bar trading name").success.value


  "DeleteAllTradingNames Controller" - {

    Seq(CheckMode, AmendMode).foreach {
      mode =>

      lazy val deleteAllTradingNamesRoute = routes.DeleteAllTradingNamesController.onPageLoad(mode).url

        s"$mode" - {

          "must return OK and the correct view for a GET" in {

            val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

            running(application) {
              val request = FakeRequest(GET, deleteAllTradingNamesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[DeleteAllTradingNamesView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, mode)(request, messages(application)).toString
            }
          }

          "must delete all trading names answers and redirect to the next page when user answers Yes" in {

            val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllTradingNamesRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value
              val expectedAnswers = userAnswers
                .set(DeleteAllTradingNamesPage, true).success.value
                .remove(AllTradingNames).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual DeleteAllTradingNamesPage.navigate(mode, expectedAnswers).url
              verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }

          "must not delete all trading names answers and redirect to the next page when user answers No" in {

            val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllTradingNamesRoute)
                  .withFormUrlEncodedBody(("value", "false"))

              val result = route(application, request).value
              val expectedAnswers = userAnswers
                .set(DeleteAllTradingNamesPage, false).success.value
                .set(HasTradingNamePage, true).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual DeleteAllTradingNamesPage.navigate(mode, expectedAnswers).url
              verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllTradingNamesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[DeleteAllTradingNamesView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, mode)(request, messages(application)).toString
            }
          }

          "must redirect to Journey Recovery for a GET if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request = FakeRequest(GET, deleteAllTradingNamesRoute)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
            }
          }

          "must redirect to Journey Recovery for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllTradingNamesRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
            }
          }
        }
    }
  }
}
