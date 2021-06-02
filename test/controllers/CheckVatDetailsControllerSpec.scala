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
import connectors.RegistrationConnector
import forms.CheckVatDetailsFormProvider
import models.UserAnswers
import models.responses
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.CheckVatDetailsPage
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.CheckVatDetailsViewModel
import views.html.{CheckVatDetailsView, CheckVatNumberView}

import scala.concurrent.Future

class CheckVatDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new CheckVatDetailsFormProvider()
  private val form = formProvider()

  private lazy val checkVatDetailsRoute = routes.CheckVatDetailsController.onPageLoad().url

  private val mockConnector  = mock[RegistrationConnector]
  private val mockRepository = mock[SessionRepository]

  private def appBuilder(answers: Option[UserAnswers]) =
    applicationBuilder(answers)
      .overrides(
        bind[RegistrationConnector].toInstance(mockConnector),
        bind[SessionRepository].toInstance(mockRepository)
      )

  override def beforeEach(): Unit = {
    Mockito.reset(mockConnector)
    Mockito.reset(mockRepository)
  }

  "CheckVatDetails Controller" - {

    ".onPageLoad" - {

      "when the user has not already answered this question" - {

        "and we can find their VAT details" - {

          "must create user answers with their VAT details, return OK and the Check Vat Details view" in {

            val application = appBuilder(answers = None).build()

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatInfo))
            when(mockRepository.set(any())) thenReturn Future.successful(true)

            running(application) {
              val request = FakeRequest(GET, checkVatDetailsRoute)
              val view = application.injector.instanceOf[CheckVatDetailsView]
              val viewModel = CheckVatDetailsViewModel(vrn, vatInfo)

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, viewModel)(request, messages(application)).toString
              verify(mockRepository, times(1)).set(eqTo(emptyUserAnswersWithVatInfo))
            }
          }
        }

        "and we cannot find their VAT details" - {

          "must create user answers with no VAT details, return OK and the Check Vat Number view" in {

            val application = appBuilder(answers = None).build()

            when(mockConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Left(responses.NotFound))
            when(mockRepository.set(any())) thenReturn Future.successful(true)

            running(application) {
              val request = FakeRequest(GET, checkVatDetailsRoute)
              val view = application.injector.instanceOf[CheckVatNumberView]

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form, vrn)(request, messages(application)).toString
              verify(mockRepository, times(1)).set(eqTo(emptyUserAnswers))
            }
          }
        }
      }

      "when the user has already answered this question" - {

        "and we had found their VAT details" - {

          "must not make another call to get details nor update user answers, and must return OK and the Check Vat Details view" in {

            val answers     = emptyUserAnswersWithVatInfo.set(CheckVatDetailsPage, true).success.value
            val application = appBuilder(answers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, checkVatDetailsRoute)
              val view = application.injector.instanceOf[CheckVatDetailsView]
              val viewModel = CheckVatDetailsViewModel(vrn, vatInfo)

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), viewModel)(request, messages(application)).toString
              verify(mockConnector, never()).getVatCustomerInfo()(any())
              verify(mockRepository, never()).set(any())
            }
          }
        }

        "and we had not found their VAT details" - {

          "must not make another call to get details nor update user answers, and must return OK and the Check Vat Number view" in {

            val answers     = emptyUserAnswers.set(CheckVatDetailsPage, true).success.value
            val application = appBuilder(answers = Some(answers)).build()

            running(application) {
              val request = FakeRequest(GET, checkVatDetailsRoute)
              val view = application.injector.instanceOf[CheckVatNumberView]

              val result = route(application, request).value

              status(result) mustEqual OK
              contentAsString(result) mustEqual view(form.fill(true), vrn)(request, messages(application)).toString
              verify(mockConnector, never()).getVatCustomerInfo()(any())
              verify(mockRepository, never()).set(any())
            }
          }
        }
      }
    }

    ".onSubmit" - {

      "when the user hasn't answered any questions" - {

        "must redirect to JourneyRecovery" in {

          val application = appBuilder(None).build()

          running(application) {
            val request =
              FakeRequest(POST, checkVatDetailsRoute)
                .withFormUrlEncodedBody(("value", "true"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }
      }

      "when we found the user's VAT details" - {

        "and the user answers the question" - {

          "must save the answer and redirect to the next page" in {

            when(mockRepository.set(any())) thenReturn Future.successful(true)

            val application =
              appBuilder(Some(emptyUserAnswersWithVatInfo))
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, checkVatDetailsRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url

              val expectedUpdatedAnswers = emptyUserAnswersWithVatInfo.set(CheckVatDetailsPage, true).success.value
              verify(mockRepository, times(1)).set(eqTo(expectedUpdatedAnswers))
            }
          }
        }

        "and the user submits a bad request" - {

          "must return Bad Request and the Check Vat Details page" in {
            val application = appBuilder(Some(emptyUserAnswersWithVatInfo)).build

            running(application) {
              val request =
                FakeRequest(POST, checkVatDetailsRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view      = application.injector.instanceOf[CheckVatDetailsView]
              val viewModel = CheckVatDetailsViewModel(vrn, vatInfo)

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, viewModel)(request, messages(application)).toString
            }
          }
        }
      }

      "when we didn't find the user's VAT details" - {

        "and the user answers the question" - {

          "must save the answer and redirect to the next page" in {

            when(mockRepository.set(any())) thenReturn Future.successful(true)

            val application =
              appBuilder(Some(emptyUserAnswers))
                .overrides(bind[Navigator].toInstance(new FakeNavigator(onwardRoute)))
                .build()

            running(application) {
              val request =
                FakeRequest(POST, checkVatDetailsRoute)
                  .withFormUrlEncodedBody(("value", "true"))

              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual onwardRoute.url

              val expectedUpdatedAnswers = emptyUserAnswers.set(CheckVatDetailsPage, true).success.value
              verify(mockRepository, times(1)).set(eqTo(expectedUpdatedAnswers))
            }
          }
        }

        "and the user submits a bad request" - {

          "must return Bad Request and the Check Vat Details page" in {

            val application = appBuilder(Some(emptyUserAnswers)).build

            running(application) {
              val request =
                FakeRequest(POST, checkVatDetailsRoute)
                  .withFormUrlEncodedBody(("value", ""))

              val boundForm = form.bind(Map("value" -> ""))

              val view      = application.injector.instanceOf[CheckVatNumberView]

              val result = route(application, request).value

              status(result) mustEqual BAD_REQUEST
              contentAsString(result) mustEqual view(boundForm, vrn)(request, messages(application)).toString
            }
          }
        }
      }
    }
  }
}
