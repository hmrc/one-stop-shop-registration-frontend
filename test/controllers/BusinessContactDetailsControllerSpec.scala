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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import forms.BusinessContactDetailsFormProvider
import models.emailVerification.EmailVerificationResponse
import models.emailVerification.PasscodeAttemptsStatus.{LockedPasscodeForSingleEmail, LockedTooManyLockedEmails, NotVerified, Verified}
import models.iossRegistration.IossEtmpDisplayRegistration
import models.responses.UnexpectedResponseStatus
import models.{AmendMode, BusinessContactDetails, NormalMode, RejoinMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalacheck.Gen
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.BusinessContactDetailsPage
import play.api.data.Form
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.{EmailVerificationService, SaveForLaterService}
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps
import views.html.BusinessContactDetailsView

class BusinessContactDetailsControllerSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val formProvider = new BusinessContactDetailsFormProvider()
  private val form = formProvider()

  private lazy val businessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
  private lazy val amendBusinessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(AmendMode).url

  private val userAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

  private val mockEmailVerificationService = mock[EmailVerificationService]
  private val mockSaveForLaterService = mock[SaveForLaterService]
  private val mockRegistrationConnector = mock[RegistrationConnector]

  private val emailVerificationResponse: EmailVerificationResponse = EmailVerificationResponse(
    redirectUri = routes.BankDetailsController.onPageLoad(NormalMode).url
  )

  private val amendEmailVerificationResponse: EmailVerificationResponse = EmailVerificationResponse(
    redirectUri = controllers.amend.routes.ChangeYourRegistrationController.onPageLoad().url
  )

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  override def beforeEach(): Unit = {
    Mockito.reset(mockEmailVerificationService)
    Mockito.reset(mockSaveForLaterService)
    Mockito.reset(mockRegistrationConnector)
  }

  "BusinessContactDetails Controller" - {

    "GET" - {

      "must return OK and the correct view for a GET" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(form, NormalMode, enrolmentsEnabled = false, None, 0)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when enrolments enabled" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .configure("features.enrolments-enabled" -> "true")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(form, NormalMode, enrolmentsEnabled = true, None, 0)(request, messages(application)).toString
        }
      }

      "must populate the view correctly on a GET when the question has previously been answered" in {

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(form.fill(contactDetails), NormalMode, enrolmentsEnabled = false, None, 0)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when an IOSS Registration is present" in {

        val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
          iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

        val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
          fullName = nonExcludedIossEtmpDisplayRegistration.schemeDetails.contactName,
          telephoneNumber = nonExcludedIossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
          emailAddress = nonExcludedIossEtmpDisplayRegistration.schemeDetails.businessEmailId
        )

        val updatedForm: Form[BusinessContactDetails] = form.fill(iossBusinessDetails)

        val application = applicationBuilder(
          userAnswers = Some(basicUserAnswersWithVatInfo),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
          numberOfIossRegistrations = 1
        )
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            updatedForm,
            NormalMode,
            enrolmentsEnabled = false,
            Some(nonExcludedIossEtmpDisplayRegistration),
            1
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when an excluded IOSS Registration is present" in {

        val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
          fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
          telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
          emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
        )

        val updatedForm = form.fill(iossBusinessDetails)

        val application = applicationBuilder(
          userAnswers = Some(basicUserAnswersWithVatInfo),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
          numberOfIossRegistrations = 1
        )
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            updatedForm,
            NormalMode,
            enrolmentsEnabled = false,
            Some(iossEtmpDisplayRegistration),
            1
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when multiple IOSS Registrations are present" in {

        val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
          fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
          telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
          emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
        )

        val updatedForm = form.fill(iossBusinessDetails)

        val application = applicationBuilder(
          userAnswers = Some(basicUserAnswersWithVatInfo),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
          numberOfIossRegistrations = 2
        )
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, businessContactDetailsRoute)

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            updatedForm,
            NormalMode,
            enrolmentsEnabled = false,
            Some(iossEtmpDisplayRegistration),
            2
          )(request, messages(application)).toString
        }
      }

      Seq(AmendMode, RejoinMode).foreach { mode =>

        lazy val businessContactDetailsRoute = routes.BusinessContactDetailsController.onPageLoad(mode).url

        s"in $mode" - {

          s"must return OK and the correct view for a GET when an IOSS Registration is present when in $mode" in {

            val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
              iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

            val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
              fullName = nonExcludedIossEtmpDisplayRegistration.schemeDetails.contactName,
              telephoneNumber = nonExcludedIossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
              emailAddress = nonExcludedIossEtmpDisplayRegistration.schemeDetails.businessEmailId
            )

            val updatedForm: Form[BusinessContactDetails] = form.fill(iossBusinessDetails)

            val application = applicationBuilder(
              userAnswers = Some(basicUserAnswersWithVatInfo),
              iossNumber = Some(iossNumber),
              iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
              numberOfIossRegistrations = 1
            )
              .configure("features.enrolments-enabled" -> "false")
              .build()

            running(application) {
              val request = FakeRequest(GET, businessContactDetailsRoute)

              val view = application.injector.instanceOf[BusinessContactDetailsView]

              val result = route(application, request).value

              status(result) `mustBe` OK
              contentAsString(result) `mustBe` view(
                updatedForm,
                mode,
                enrolmentsEnabled = false,
                Some(nonExcludedIossEtmpDisplayRegistration),
                1
              )(request, messages(application)).toString
            }
          }

          s"must return OK and the correct view for a GET when an excluded IOSS Registration is present when in $mode" in {

            val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
              fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
              telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
              emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
            )

            val updatedForm = form.fill(iossBusinessDetails)

            val application = applicationBuilder(
              userAnswers = Some(basicUserAnswersWithVatInfo),
              iossNumber = Some(iossNumber),
              iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
              numberOfIossRegistrations = 1
            )
              .configure("features.enrolments-enabled" -> "false")
              .build()

            running(application) {
              val request = FakeRequest(GET, businessContactDetailsRoute)

              val view = application.injector.instanceOf[BusinessContactDetailsView]

              val result = route(application, request).value

              status(result) `mustBe` OK
              contentAsString(result) `mustBe` view(
                updatedForm,
                mode,
                enrolmentsEnabled = false,
                Some(iossEtmpDisplayRegistration),
                1
              )(request, messages(application)).toString
            }
          }

          s"must return OK and the correct view for a GET when multiple IOSS Registrations are present when in $mode" in {

            val iossBusinessDetails: BusinessContactDetails = BusinessContactDetails(
              fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
              telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
              emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
            )

            val updatedForm = form.fill(iossBusinessDetails)

            val application = applicationBuilder(
              userAnswers = Some(basicUserAnswersWithVatInfo),
              iossNumber = Some(iossNumber),
              iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
              numberOfIossRegistrations = 2
            )
              .configure("features.enrolments-enabled" -> "false")
              .build()

            running(application) {
              val request = FakeRequest(GET, businessContactDetailsRoute)

              val view = application.injector.instanceOf[BusinessContactDetailsView]

              val result = route(application, request).value

              status(result) `mustBe` OK
              contentAsString(result) `mustBe` view(
                updatedForm,
                mode,
                enrolmentsEnabled = false,
                Some(iossEtmpDisplayRegistration),
                2
              )(request, messages(application)).toString
            }
          }
        }
      }
    }

    "onSubmit" - {

      "when email verification enabled" - {

        "must save the answer and redirect to the next page if email is already verified and valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Verified.toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
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
            val expectedAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` routes.BankDetailsController.onPageLoad(NormalMode).url
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

        "must save the answer and redirect to the next page if email is already verified and valid data is submitted in amend mode" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(RegistrationData.registration).toFuture
          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn Verified.toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
                bind[EmailVerificationService].toInstance(mockEmailVerificationService),
                bind[RegistrationConnector].toInstance(mockRegistrationConnector)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, amendBusinessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value
            val expectedAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` controllers.amend.routes.ChangeYourRegistrationController.onPageLoad().url
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(emailVerificationRequest.email.get.address), eqTo(emailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(0))
              .createEmailVerificationRequest(
                eqTo(AmendMode),
                eqTo(emailVerificationRequest.credId),
                eqTo(emailVerificationRequest.email.get.address),
                eqTo(emailVerificationRequest.pageTitle),
                eqTo(emailVerificationRequest.continueUrl))(any())
          }
        }

        "must save the answer and redirect to the Business Contact Details page if email is not verified and valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn NotVerified.toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
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
            val expectedAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

            val anEmailVerificationRequest = emailVerificationRequest.copy(
              continueUrl = s"${config.loginContinueUrl}${emailVerificationRequest.continueUrl}"
            )

            when(mockEmailVerificationService.isEmailVerified(
              eqTo(anEmailVerificationRequest.email.get.address),
              eqTo(anEmailVerificationRequest.credId))(any())) thenReturn NotVerified.toFuture

            when(mockEmailVerificationService.createEmailVerificationRequest(
              eqTo(NormalMode),
              eqTo(anEmailVerificationRequest.credId),
              eqTo(anEmailVerificationRequest.email.get.address),
              eqTo(anEmailVerificationRequest.pageTitle),
              eqTo(anEmailVerificationRequest.continueUrl))(any())) thenReturn Right(emailVerificationResponse).toFuture

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` config.emailVerificationUrl + emailVerificationResponse.redirectUri
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(anEmailVerificationRequest.email.get.address), eqTo(anEmailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(1))
              .createEmailVerificationRequest(
                eqTo(NormalMode),
                eqTo(anEmailVerificationRequest.credId),
                eqTo(anEmailVerificationRequest.email.get.address),
                eqTo(anEmailVerificationRequest.pageTitle),
                eqTo(anEmailVerificationRequest.continueUrl))(any())

          }
        }

        "must save the answer and redirect to the Business Contact Details page if email is not verified and valid data is submitted in amend mode" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          val newEmailAddress = "email@example.co.uk"

          when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(RegistrationData.registration).toFuture
          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(mockEmailVerificationService.isEmailVerified(
            any(),
            any())(any())) thenReturn NotVerified.toFuture
          when(mockEmailVerificationService.createEmailVerificationRequest(
            any(),
            any(),
            any(),
            any(),
            any())(any())) thenReturn Right(amendEmailVerificationResponse).toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo), mode = Some(AmendMode))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
                bind[EmailVerificationService].toInstance(mockEmailVerificationService),
                bind[RegistrationConnector].toInstance(mockRegistrationConnector)
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, amendBusinessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", newEmailAddress))

            val config = application.injector.instanceOf[FrontendAppConfig]

            val amendEmailVerificationRequest = emailVerificationRequest.copy(
              email = emailVerificationRequest.email.map(_.copy(address = newEmailAddress)),
              continueUrl = s"${config.loginContinueUrl}${controllers.amend.routes.ChangeYourRegistrationController.onPageLoad().url}"
            )

            val result = route(application, request).value
            val expectedAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails.copy(emailAddress = newEmailAddress)).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` config.emailVerificationUrl + amendEmailVerificationResponse.redirectUri
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(newEmailAddress), eqTo(amendEmailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(1))
              .createEmailVerificationRequest(
                eqTo(AmendMode),
                eqTo(amendEmailVerificationRequest.credId),
                eqTo(amendEmailVerificationRequest.email.get.address),
                eqTo(amendEmailVerificationRequest.pageTitle),
                eqTo(amendEmailVerificationRequest.continueUrl))(any())
          }
        }

        "must save the answer and redirect to the Email Verification Codes Exceeded page if valid data is submitted but" +
          " verification attempts on a single email are exceeded" in {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn LockedPasscodeForSingleEmail.toFuture
          when(mockSaveForLaterService.saveAnswers(any(), any())(any(), any(), any())) thenReturn
            Redirect(routes.EmailVerificationCodesExceededController.onPageLoad()).toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
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

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` routes.EmailVerificationCodesExceededController.onPageLoad().url
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

        "must save the answer and redirect to the Email Verification Codes and Emails Exceeded page if valid data is submitted but" +
          " verification attempts on maximum emails are exceeded" in {

          when(mockEmailVerificationService.isEmailVerified(
            eqTo(emailVerificationRequest.email.get.address),
            eqTo(emailVerificationRequest.credId))(any())) thenReturn LockedTooManyLockedEmails.toFuture
          when(mockSaveForLaterService.saveAnswers(any(), any())(any(), any(), any())) thenReturn
            Redirect(routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad()).toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
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

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` routes.EmailVerificationCodesAndEmailsExceededController.onPageLoad().url
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

          when(mockSessionRepository.set(any())) thenReturn true.toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "true")
              .configure("features.enrolments-enabled" -> "false")
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

            val anEmailVerificationRequest = emailVerificationRequest.copy(
              continueUrl = s"${config.loginContinueUrl}${emailVerificationRequest.continueUrl}"
            )

            when(mockEmailVerificationService.isEmailVerified(
              eqTo(anEmailVerificationRequest.email.get.address),
              eqTo(anEmailVerificationRequest.credId))(any())) thenReturn NotVerified.toFuture

            when(mockEmailVerificationService.createEmailVerificationRequest(
              eqTo(NormalMode),
              eqTo(anEmailVerificationRequest.credId),
              eqTo(anEmailVerificationRequest.email.get.address),
              eqTo(anEmailVerificationRequest.pageTitle),
              eqTo(anEmailVerificationRequest.continueUrl))(any())) thenReturn
              Left(UnexpectedResponseStatus(httpStatus, "error")).toFuture

            val result = route(application, request).value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` routes.BusinessContactDetailsController.onPageLoad(NormalMode).url
            verifyNoInteractions(mockSessionRepository)
            verify(mockEmailVerificationService, times(1))
              .isEmailVerified(eqTo(anEmailVerificationRequest.email.get.address), eqTo(anEmailVerificationRequest.credId))(any())
            verify(mockEmailVerificationService, times(1))
              .createEmailVerificationRequest(
                eqTo(NormalMode),
                eqTo(anEmailVerificationRequest.credId),
                eqTo(anEmailVerificationRequest.email.get.address),
                eqTo(anEmailVerificationRequest.pageTitle),
                eqTo(anEmailVerificationRequest.continueUrl))(any())
          }
        }
      }

      "when email verification disabled" - {

        "must save the answer and redirect to the next page when valid data is submitted" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn true.toFuture

          val application =
            applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
              .configure("features.email-verification-enabled" -> "false")
              .configure("features.enrolments-enabled" -> "false")
              .overrides(
                bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository),
              )
              .build()

          running(application) {
            val request =
              FakeRequest(POST, businessContactDetailsRoute)
                .withFormUrlEncodedBody(("fullName", "name"), ("telephoneNumber", "0111 2223334"), ("emailAddress", "email@example.com"))

            val result = route(application, request).value
            val expectedAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` routes.BankDetailsController.onPageLoad(NormalMode).url
            verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
            verifyNoInteractions(mockEmailVerificationService)
          }
        }
      }

      "must return a Bad Request and errors when invalid data is submitted" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request =
            FakeRequest(POST, businessContactDetailsRoute)
              .withFormUrlEncodedBody(("value", "invalid value"))

          val boundForm = form.bind(Map("value" -> "invalid value"))

          val view = application.injector.instanceOf[BusinessContactDetailsView]

          val result = route(application, request).value

          status(result) `mustBe` BAD_REQUEST
          contentAsString(result) `mustBe` view(boundForm, NormalMode, enrolmentsEnabled = false, None, 0)(request, messages(application)).toString
        }
      }
    }
  }
}

