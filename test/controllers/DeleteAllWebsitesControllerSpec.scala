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
import connectors.RegistrationConnector
import forms.DeleteAllWebsitesFormProvider
import models.{AmendMode, CheckMode, Index}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{DeleteAllWebsitesPage, HasWebsitePage, WebsitePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.AllWebsites
import repositories.AuthenticatedUserAnswersRepository
import testutils.RegistrationData
import utils.CheckJourneyRecovery.determineJourneyRecovery
import views.html.DeleteAllWebsitesView

import scala.concurrent.Future

class DeleteAllWebsitesControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DeleteAllWebsitesFormProvider()
  private val form = formProvider()

  private val mockRegistrationConnector = mock[RegistrationConnector]

  private val userAnswers = basicUserAnswersWithVatInfo
    .set(WebsitePage(Index(0)), "foo").success.value
    .set(WebsitePage(Index(1)), "bar").success.value

  "DeleteAllWebsites Controller" - {

    Seq(CheckMode, AmendMode).foreach {
      mode =>
        lazy val deleteAllWebsitesRoute = routes.DeleteAllWebsitesController.onPageLoad(mode).url

        s"$mode" - {

          "must return OK and the correct view for a GET" in {

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

            val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(GET, deleteAllWebsitesRoute)

              val result = route(application, request).value

              val view = application.injector.instanceOf[DeleteAllWebsitesView]

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, mode)(request, messages(application)).toString
            }
          }

          "must delete all websites answers and redirect to the next page when user answers Yes" in {

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

            val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
                .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllWebsitesRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value
              val expectedAnswers = userAnswers
                .set(DeleteAllWebsitesPage, true).success.value
                .remove(AllWebsites).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual DeleteAllWebsitesPage.navigate(mode, expectedAnswers).url
              verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }

          "must not delete all websites answers and redirect to the next page when user answers No" in {

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

            val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

            when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

            val application =
              applicationBuilder(userAnswers = Some(userAnswers))
                .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
                .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllWebsitesRoute)
                  .withFormUrlEncodedBody(("value", "false"))

              val result = route(application, request).value
              val expectedAnswers = userAnswers
                .set(DeleteAllWebsitesPage, false).success.value
                .set(HasWebsitePage, true).success.value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual DeleteAllWebsitesPage.navigate(mode, expectedAnswers).url
              verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            }
          }

          "must return a Bad Request and errors when invalid data is submitted" in {

            when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(RegistrationData.registration))

            val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
              .build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllWebsitesRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view = application.injector.instanceOf[DeleteAllWebsitesView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, mode)(request, messages(application)).toString
            }
          }

          s"must redirect to Journey Recovery in $mode for a GET if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request = FakeRequest(GET, deleteAllWebsitesRoute)

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual determineJourneyRecovery(Some(mode)).url
            }
          }

          s"must redirect to Journey Recovery in $mode for a POST if no existing data is found" in {

            val application = applicationBuilder(userAnswers = None).build()

            running(application) {
              val request =
                FakeRequest(POST, deleteAllWebsitesRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual determineJourneyRecovery(Some(mode)).url
            }
          }
        }
    }
  }
}
