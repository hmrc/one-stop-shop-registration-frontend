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
import config.FrontendAppConfig
import forms.BusinessContactDetailsFormProvider
import models.NormalMode
import models.emailVerification.EmailVerificationResponse
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.responses.UnexpectedResponseStatus
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito._
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.{EmailVerificationService, SaveForLaterService}
import views.html.BusinessContactDetailsView

import scala.concurrent.Future

class BusinessContactDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val formProvider = new BusinessContactDetailsFormProvider()
  private val form = formProvider()

  private lazy val businessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
  private val userAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val mockSaveForLaterService = mock[SaveForLaterService]

  private val emailVerificationResponse: EmailVerificationResponse = EmailVerificationResponse(
    redirectUri = routes.BankDetailsController.onPageLoad(NormalMode).url
  )

  override def beforeEach(): Unit = {
    Mockito.reset(mockEmailVerificationService)
    Mockito.reset(mockSaveForLaterService)
  }

  "BusinessContactDetails Controller" - {

    "GET" - {

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
    }

    "onSubmit" - {

      "when email verification enabled" - {

        "must save the answer and redirect to the next page if email is already verified and valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Future.successful(Verified)

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "true")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
                bind[EmailVerificationService].toInstance(mockEmailVerificationService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value
            val expectedAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.BankDetailsController.onPageLoad(NormalMode).url
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(0))
              .createEmailVerificationRequest(
                eqTo(NormalMode),
                eqTo(emailVerificationRequest.credId),
                eqTo(emailVerificationRequest.email.get.address),
                eqTo(emailVerificationRequest.pageTitle),
                eqTo(emailVerificationRequest.continueUrl))(any())
          }
        }

        "must save the answer and redirect to the Business Contact Details page if email is not verified and valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Future.successful(NotVerified)
          when(mockEmailVerificationService.createEmailVerificationRequest(
            eqTo(NormalMode),
            eqTo(emailVerificationRequest.credId),
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.pageTitle),
            eqTo(emailVerificationRequest.continueUrl))(any())) thenReturn Future.successful(Right(emailVerificationResponse))

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "true")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
                bind[EmailVerificationService].toInstance(mockEmailVerificationService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val config = application.injector.instanceOf[FrontendAppConfig]
            val result = route(application, request).value
            val expectedAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual config.emailVerificationUrl + emailVerificationResponse.redirectUri
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(1))
              .createEmailVerificationRequest(
                eqTo(NormalMode),
                eqTo(emailVerificationRequest.credId),
                eqTo(emailVerificationRequest.email.get.address),
                eqTo(emailVerificationRequest.pageTitle),
                eqTo(emailVerificationRequest.continueUrl))(any())
          }
        }

        "must save the answer and redirect to the Email Verification Codes Exceeded page if valid data is submitted but verification attempts on a single email are exceeded" in {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Future.successful(LockedPasscodeForSingleEmail)
          when(mockSaveForLaterService.saveAnswers(any(), any())(any(), any(), any())) thenReturn
            Future.successful(Redirect(routes.EmailVerificationCodesExceededController.onPageLoad()))

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "true")
              .overrides(
                bind[EmailVerificationService].toInstance(mockEmailVerificationService),
                bind[SaveForLaterService].toInstance(mockSaveForLaterService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.EmailVerificationCodesExceededController.onPageLoad().url
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verifyNoMoreInteractions(mockEmailVerificationService)
            verify(mockSaveForLaterService, times(1))
              .saveAnswers(
                eqTo(routes.EmailVerificationCodesExceededController.onPageLoad()),
                eqTo(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
              )(any(), any(), any())
          }
        }

        "must save the answer and redirect to the Email Verification Codes and Emails Exceeded page if valid data is submitted but verification attempts on maximum emails are exceeded" in {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Future.successful(LockedTooManyLockedEmails)
          when(mockSaveForLaterService.saveAnswers(any(), any())(any(), any(), any())) thenReturn
            Future.successful(Redirect(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad()))

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "true")
              .overrides(
                bind[EmailVerificationService].toInstance(mockEmailVerificationService),
                bind[SaveForLaterService].toInstance(mockSaveForLaterService)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verifyNoMoreInteractions(mockEmailVerificationService)
            verify(mockSaveForLaterService, times(1))
              .saveAnswers(
                eqTo(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad()),
                eqTo(routes.BusinessContactDetailsController.onPageLoad(NormalMode))
              )(any(), any(), any())
          }
        }

        "must not save the answer and redirect to the current page when invalid email is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          val httpStatus = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Future.successful(NotVerified)
          when(mockEmailVerificationService.createEmailVerificationRequest(
            eqTo(NormalMode),
            eqTo(emailVerificationRequest.credId),
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.pageTitle),
            eqTo(emailVerificationRequest.continueUrl))(any())) thenReturn
            Future.successful(Left(UnexpectedResponseStatus(httpStatus, "error")))

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "true")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
                bind[EmailVerificationService].toInstance(mockEmailVerificationService)
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
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(1))
              .createEmailVerificationRequest(
                eqTo(NormalMode),
                eqTo(emailVerificationRequest.credId),
                eqTo(emailVerificationRequest.email.get.address),
                eqTo(emailVerificationRequest.pageTitle),
                eqTo(emailVerificationRequest.continueUrl))(any())
          }
        }

      }

      "when email verification disabled" - {

        "must save the answer and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswers))
              .configure("features.email-verification-enabled" -> "false")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value
            val expectedAnswers = basicUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.BankDetailsController.onPageLoad(NormalMode).url
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verifyNoInteractions(mockEmailVerificationService)
          }
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

}
