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
import pages.euDetails.{EuCountryPage, EuTaxReferencePage, TaxRegisteredInEuPage}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviouslyRegisteredPage}
import pages._
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, _}
import queries.EmailConfirmationQuery
import repositories.AuthenticatedUserAnswersRepository
import services.{AuditService, DateService, EmailService, RegistrationService}
import testutils.RegistrationData
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

import java.time.LocalDate
import scala.concurrent.Future

class CheckYourAnswersControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val registration = RegistrationData.registration

  private val registrationService = mock[RegistrationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val emailService = mock[EmailService]
  private val auditService = mock[AuditService]
  private val dateService = mock[DateService]
  private val country = arbitraryCountry.arbitrary.sample.value
  private val commencementDate = LocalDate.of(2022, 1, 1)

  override def beforeEach(): Unit = {
    Mockito.reset(
      registrationConnector,
      registrationService,
      auditService,
      emailService,
      dateService
    )
  }

  "Check Your Answers Controller" - {

    "GET" - {
      "must return OK and the correct view when answers are complete" in {
        val commencementDate = LocalDate.of(2022, 1, 1)
        when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
        when(dateService.startOfNextQuarter) thenReturn (commencementDate)

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[DateService].toInstance(dateService))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersView]
          implicit val msgs: Messages = messages(application)
          val list = SummaryListViewModel(rows = getCYASummaryList(completeUserAnswers, dateService))

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, true)(request, messages(application)).toString
        }
      }

      "must return OK and view with invalid prompt when" - {
        "trading name is missing" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))


            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "websites are missing" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers.set(HasWebsitePage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "eligible sales is not populated correctly" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "tax registered in eu is not populated correctly" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "previous registrations is not populated correctly" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "tax registered in eu has a country with missing data" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers
            .set(TaxRegisteredInEuPage, true).success.value
            .set(EuCountryPage(Index(0)), country).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }
        }

        "previous registrations has a country with missing data" in {
          when(dateService.startDateBasedOnFirstSale(any())) thenReturn (commencementDate)
          when(dateService.startOfNextQuarter) thenReturn (commencementDate)

          val answers = completeUserAnswers
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), country).success.value
          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[CheckYourAnswersView]
            implicit val msgs: Messages = messages(application)
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService))

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(list, isValid = false)(request, messages(application)).toString
          }

        }
      }
    }

    "on submit" - {

      "when the user has answered all necessary data and submission of the registration succeeds" - {

        "must audit the event and redirect to the next page and successfully send email confirmation" in {
          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
          when(registrationService.fromUserAnswers(any(), any())) thenReturn Valid(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Right(()))
          doNothing().when(auditService).audit(any())(any(), any())

          val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
          val userAnswers = basicUserAnswersWithVatInfo.set(BusinessContactDetailsPage, contactDetails).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[EmailService].toInstance(emailService),
              bind[AuditService].toInstance(auditService),
              bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
            ).build()

          running(application) {
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

          val application = applicationBuilder(userAnswers = None)
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

        "the user is redirected when the incomplete prompt is shown" - {

          "to Check EU Details when one of the tax registered countries is incomplete" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers
              .set(TaxRegisteredInEuPage, true).success.value
              .set(EuCountryPage(Index(0)), country).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(CheckMode, Index(0)).url

            }

          }

          "to Previous Eu Vat Number when one of the previously registered countries is incomplete" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers
              .set(PreviouslyRegisteredPage, true).success.value
              .set(PreviousEuCountryPage(Index(0)), country).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual
                controllers.previousRegistrations.routes.PreviousEuVatNumberController.onPageLoad(CheckMode, Index(0)).url

            }

          }

          "to Has Trading Name when trading names are not populated correctly" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value
            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasTradingNameController.onPageLoad(CheckMode).url

            }

          }

          "to Has Made Sales when eligible sales are not populated correctly" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasMadeSalesController.onPageLoad(CheckMode).url

            }

          }

          "to Has Website when websites are not populated correctly" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers.set(HasWebsitePage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasWebsiteController.onPageLoad(CheckMode).url

            }

          }

          "to Tax Registered In Eu when eu details are not populated correctly" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(CheckMode).url

            }

          }

          "to Previously Registered when previous registrations are not populated correctly" in {
            when(registrationService.fromUserAnswers(any(), any())) thenReturn Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0)))))
            val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationService].toInstance(registrationService)).build()

            running(application) {
              val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.previousRegistrations.routes.PreviouslyRegisteredController.onPageLoad(CheckMode).url

            }

          }
        }
      }

      "when the submission fails because the user has already registered" - {

        "the user is redirected to Already Registered Page" in {

          when(registrationService.fromUserAnswers(any(), any())) thenReturn Valid(registration)
          when(registrationConnector.submitRegistration(any())(any())) thenReturn Future.successful(Left(ConflictFound))
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(basicUserAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, basicUserAnswers)
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

          val application = applicationBuilder(userAnswers = Some(basicUserAnswers))
            .overrides(
              bind[RegistrationService].toInstance(registrationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, routes.CheckYourAnswersController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, basicUserAnswers)
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
