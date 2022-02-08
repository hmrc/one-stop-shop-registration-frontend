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
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import connectors.RegistrationConnector
import models.audit.{RegistrationAuditModel, SubmissionResult}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.AuthenticatedDataRequest
import models.responses.{ConflictFound, UnexpectedResponseStatus}
import models.{BusinessContactDetails, CheckMode, DataMissingError, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{doNothing, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.EuTaxReferencePage
import pages.{BusinessContactDetailsPage, CheckYourAnswersPage, HasWebsitePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, _}
import queries.EmailConfirmationQuery
import repositories.AuthenticatedSessionRepository
import services.{AuditService, DateService, EmailService, RegistrationService}
import testutils.RegistrationData
import viewmodels.checkAnswers.euDetails.TaxRegisteredInEuSummary
import viewmodels.checkAnswers.previousRegistrations.PreviouslyRegisteredSummary
import viewmodels.checkAnswers._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val registration = RegistrationData.registration

  private val registrationService = mock[RegistrationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val emailService = mock[EmailService]
  private val auditService = mock[AuditService]

  override def beforeEach(): Unit = {
    Mockito.reset(
      registrationConnector,
      registrationService,
      auditService,
      emailService
    )
  }

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(completeUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: Messages = messages(application)
        val list = SummaryListViewModel(rows = Seq(
          RegisteredCompanyNameSummary.row(completeUserAnswers),
          PartOfVatGroupSummary.row(completeUserAnswers),
          UkVatEffectiveDateSummary.row(completeUserAnswers),
          BusinessAddressInUkSummary.row(completeUserAnswers),
          UkAddressSummary.row(completeUserAnswers),
          InternationalAddressSummary.row(completeUserAnswers),
          new HasTradingNameSummary().row(completeUserAnswers),
          HasMadeSalesSummary.row(completeUserAnswers),
          IsPlanningFirstEligibleSaleSummary.row(completeUserAnswers),
          TaxRegisteredInEuSummary.row(completeUserAnswers),
          PreviouslyRegisteredSummary.row(completeUserAnswers),
          IsOnlineMarketplaceSummary.row(completeUserAnswers),
          HasWebsiteSummary.row(completeUserAnswers),
          BusinessContactDetailsSummary.row(completeUserAnswers),
          BankDetailsSummary.row(completeUserAnswers)
        ).flatten)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, true)(request, messages(application)).toString
      }
    }

    "must redirect to corresponding url when no data is present when validated" in {

      val application = applicationBuilder(userAnswers = Some(invalidUserAnswers))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckYourAnswersView]
        implicit val msgs: Messages = messages(application)
        val list = SummaryListViewModel(rows = Seq(
          RegisteredCompanyNameSummary.row(completeUserAnswers),
          PartOfVatGroupSummary.row(completeUserAnswers),
          UkVatEffectiveDateSummary.row(completeUserAnswers),
          BusinessAddressInUkSummary.row(completeUserAnswers),
          UkAddressSummary.row(completeUserAnswers),
          InternationalAddressSummary.row(completeUserAnswers),
          new HasTradingNameSummary().row(completeUserAnswers),
          HasMadeSalesSummary.row(completeUserAnswers),
          IsPlanningFirstEligibleSaleSummary.row(completeUserAnswers),
          TaxRegisteredInEuSummary.row(invalidUserAnswers),
          PreviouslyRegisteredSummary.row(completeUserAnswers),
          IsOnlineMarketplaceSummary.row(completeUserAnswers),
          HasWebsiteSummary.row(completeUserAnswers),
          BusinessContactDetailsSummary.row(completeUserAnswers),
          BankDetailsSummary.row(completeUserAnswers)
        ).flatten)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, false)(request, messages(application)).toString
      }
    }

    "on submit" - {

      "when the user has answered all necessary data and submission of the registration succeeds" - {

        "must audit the event and redirect to the next page and successfully send email confirmation" in {
          val mockSessionRepository = mock[AuthenticatedSessionRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(registrationService.fromUserAnswers(any(), any())) thenReturn Valid(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Right(()))
          doNothing().when(auditService).audit(any())(any(), any())

          val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
          val userAnswers = emptyUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[EmailService].toInstance(emailService),
              bind[AuditService].toInstance(auditService),
              bind[AuthenticatedSessionRepository].toInstance(mockSessionRepository)
            ).build()

          running(application) {
            val dateService = application.injector.instanceOf[DateService]
            when(emailService.sendConfirmationEmail(
              eqTo(registration.contactDetails.fullName),
              eqTo(registration.registeredCompanyName),
              eqTo(vrn.toString()),
              eqTo(registration.commencementDate),
              eqTo(registration.contactDetails.emailAddress),
              eqTo(registration.dateOfFirstSale)
            )(any())) thenReturn Future.successful(EMAIL_ACCEPTED)

            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, userAnswers)
            val expectedAuditEvent = RegistrationAuditModel.build(registration, SubmissionResult.Success, dataRequest)
            val userAnswersWithEmailConfirmation = userAnswers.copy().set(EmailConfirmationQuery, true).success.value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual CheckYourAnswersPage.navigate(NormalMode, userAnswersWithEmailConfirmation).url

            verify(emailService, times(1))
              .sendConfirmationEmail(any(), any(), any(), any(), any(), any())(any())
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
            verify(mockSessionRepository, times(1)).set(eqTo(userAnswersWithEmailConfirmation))
          }
        }
      }

      "when the user has not answered all necessary data" - {

        "the user is redirected to Journey Recovery Page" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(HasWebsitePage)))

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(bind[RegistrationService].toInstance(registrationService)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
          }
        }

        "the page is refreshed when the incomplete prompt was not shown" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))

          val application = applicationBuilder(userAnswers = Some(invalidUserAnswers))
            .overrides(bind[RegistrationService].toInstance(registrationService)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url
          }
        }

        "the user is redirected when the incomplete prompt is shown" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))

          val application = applicationBuilder(userAnswers = Some(invalidUserAnswers))
            .overrides(bind[RegistrationService].toInstance(registrationService)).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
            val result = route(application, request).value

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(0)).url
          }
        }
      }

      "when the submission fails because the user has already registered" - {

        "the user is redirected to Already Registered Page" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Valid(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Left(ConflictFound))
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, emptyUserAnswers)
            val expectedAuditEvent = RegistrationAuditModel.build(registration, SubmissionResult.Duplicate, dataRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.AlreadyRegisteredController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }

      "when the submission fails because of a technical issue" - {

        "the user is redirected to the Journey Recovery page" in {

          val errorResponse = UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")
          when(registrationService.fromUserAnswers(any(), any())) thenReturn Valid(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Left(errorResponse))
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, emptyUserAnswers)
            val expectedAuditEvent = RegistrationAuditModel.build(registration, SubmissionResult.Failure, dataRequest)

            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }

    }
  }
}
