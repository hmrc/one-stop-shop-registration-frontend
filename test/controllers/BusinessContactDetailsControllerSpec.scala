/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.EmailVerificationConnector
import forms.BusinessContactDetailsFormProvider
import models.responses.UnexpectedResponseStatus
import models.NormalMode
import models.emailVerification.EmailVerificationResponse
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.BusinessContactDetailsView

import scala.concurrent.Future

class BusinessContactDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new BusinessContactDetailsFormProvider()
  private val form = formProvider()

  private lazy val businessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(NormalMode).url

  private val userAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

  private val mockEmailVerificationConnector = mock[EmailVerificationConnector]

  private val emailVerificationResponse: EmailVerificationResponse = EmailVerificationResponse(
    redirectUri = routes.BankDetailsController.onPageLoad(NormalMode).url
  )

  "BusinessContactDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(contactDetails), NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockEmailVerificationConnector.verifyEmail(eqTo(emailVerificationRequest))(any(), any())) thenReturn
        Future.successful(Right(emailVerificationResponse))

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
            bind[EmailVerificationConnector].toInstance(mockEmailVerificationConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual emailVerificationResponse.redirectUri
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
//TODO        verify(mockEmailVerificationConnector.verifyEmail(eqTo(emailVerificationRequest))(any(), any()), times(1))
      }
    }

    "must not save the answer and redirect to the current page when invalid email is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      val httpStatus = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
      when(mockEmailVerificationConnector.verifyEmail(any())(any(), any())) thenReturn
        Future.successful(Left(UnexpectedResponseStatus(httpStatus, "error")))

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
            bind[EmailVerificationConnector].toInstance(mockEmailVerificationConnector)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
        verifyNoInteractions(mockSessionRepository)
//TODO        verify(mockEmailVerificationConnector, times(1)).verifyEmail(eqTo(emailVerificationRequest)).futureValue
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[BusinessContactDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, enrolmentsEnabled = false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, businessContactDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, businessContactDetailsRoute)
            .withFormUrlEncodedBody(("fullName", "value 1"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@email.com"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
