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

package controllers.amend

import base.SpecBase
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import connectors.RegistrationConnector
import controllers.amend.routes as amendRoutes
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.emails.EmailSendingResult.EMAIL_ACCEPTED
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import models.responses.UnexpectedResponseStatus
import models.{AmendMode, BusinessContactDetails, DataMissingError, Index, NormalMode, PreviousScheme, PreviousSchemeType}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.*
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import pages.amend.ChangeYourRegistrationPage
import pages.euDetails.{EuCountryPage, EuTaxReferencePage, TaxRegisteredInEuPage}
import pages.previousRegistrations.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.OFormat.oFormatFromReadsAndOWrites
import play.api.test.FakeRequest
import play.api.test.Helpers.{running, *}
import queries.{EmailConfirmationQuery, EuDetailsQuery}
import repositories.AuthenticatedUserAnswersRepository
import services.*
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps
import viewmodels.govuk.SummaryListFluency
import views.html.amend.ChangeYourRegistrationView

import java.time.LocalDate

class ChangeYourRegistrationControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val registration = RegistrationData.registration
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, Some(registration), emptyUserAnswers, None, 0, None)
  private val dataRequest: AuthenticatedMandatoryDataRequest[_] = AuthenticatedMandatoryDataRequest(request, testCredentials, vrn, registration, emptyUserAnswers)

  private val registrationValidationService = mock[RegistrationValidationService]
  private val registrationService = mock[RegistrationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val emailService = mock[EmailService]
  private val auditService = mock[AuditService]
  private val dateService = mock[DateService]
  private val country = arbitraryCountry.arbitrary.sample.value
  private val commencementDate = LocalDate.of(2022, 1, 1)

  override def beforeEach(): Unit = {
    reset(registrationConnector)
    reset(registrationValidationService)
    reset(auditService)
    reset(emailService)
    reset(dateService)
  }

  "Change Your Registration Controller" - {

    "GET" - {

      "must return OK and the correct view when answers are complete" in {

        val commencementDate = LocalDate.of(2022, 1, 1)
        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
        when(dateService.startOfNextQuarter()) thenReturn commencementDate
        when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

        val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
          .overrides(bind[DateService].toInstance(dateService))
          .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
          val result = route(application, request).value
          val view = application.injector.instanceOf[ChangeYourRegistrationView]
          implicit val msgs: Messages = messages(application)
          val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(completeUserAnswers))
          val list = SummaryListViewModel(rows = getCYASummaryList(completeUserAnswers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = true, AmendMode)(request, messages(application)).toString
        }
      }

      "must return OK and view with invalid prompt when" - {

        "trading name is missing" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)


            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "websites are missing" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasWebsitePage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "eligible sales is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "tax registered in eu is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "previous registrations is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "tax registered in eu has a country with missing data" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers
            .set(TaxRegisteredInEuPage, true).success.value
            .set(EuCountryPage(Index(0)), country).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }

        "previous registrations has a country with missing data" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(commencementDate).toFuture
          when(dateService.startOfNextQuarter()) thenReturn (commencementDate)
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), country).success.value
          val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

          running(application) {
            val request = FakeRequest(GET, amendRoutes.ChangeYourRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[ChangeYourRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, AmendMode)(request = dataRequest.request).futureValue)

            status(result) `mustBe` OK
            contentAsString(result) `mustBe` view(vatRegistrationDetailsList, list, isValid = false, AmendMode)(request, messages(application)).toString
          }
        }
      }
    }

    "on submit" - {

      "when the user has answered all necessary data and submission of the registration succeeds" - {

        "must audit the event and redirect to the next page and successfully send email confirmation when enrolment is not enabled" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Valid(registration).toFuture
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Right(()).toFuture
          doNothing().when(auditService).audit(any())(any(), any())

          val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
          val userAnswers = completeUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers), registration = Some(registration))
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[EmailService].toInstance(emailService),
              bind[AuditService].toInstance(auditService),
              bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
            ).build()

          running(application) {
            when(emailService.sendConfirmationEmail(
              eqTo(registration.contactDetails.fullName),
              eqTo(registration.registeredCompanyName),
              eqTo(registration.commencementDate),
              eqTo(registration.contactDetails.emailAddress),
              eqTo(AmendMode)
            )(any(), any())) thenReturn EMAIL_ACCEPTED.toFuture

            val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, userAnswers, None, 0, None)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Success, dataRequest)
            val userAnswersWithEmailConfirmation = userAnswers.copy().set(EmailConfirmationQuery, true).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` ChangeYourRegistrationPage.navigate(NormalMode, userAnswersWithEmailConfirmation).url

            verify(emailService, times(1))
              .sendConfirmationEmail(any(), any(), any(), any(), any())(any(), any())
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
            verify(mockSessionRepository, times(1)).set(eqTo(userAnswersWithEmailConfirmation))
          }
        }

        "must audit the event and redirect to the next page and not send email confirmation when send email is disabled" in {

          val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

          when(mockSessionRepository.set(any())) thenReturn true.toFuture
          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Valid(registration).toFuture
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Right(()).toFuture
          doNothing().when(auditService).audit(any())(any(), any())

          val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
          val userAnswers = completeUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

          val application = applicationBuilder(userAnswers = Some(userAnswers), registration = Some(registration))
            .configure("features.amend.email-enabled" -> "false")
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[EmailService].toInstance(emailService),
              bind[AuditService].toInstance(auditService),
              bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
            ).build()

          running(application) {

            val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, userAnswers, None, 0, None)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Success, dataRequest)
            val userAnswersWithEmailConfirmation = userAnswers.copy().set(EmailConfirmationQuery, false).success.value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` ChangeYourRegistrationPage.navigate(NormalMode, userAnswersWithEmailConfirmation).url

            verifyNoInteractions(emailService)
            verify(mockSessionRepository, times(1)).set(eqTo(userAnswersWithEmailConfirmation))
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }

      "when the user has not answered all necessary data" - {

        "the page is refreshed when the incomplete prompt was not shown" in {

          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
            Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

          val application = applicationBuilder(userAnswers = Some(invalidUserAnswers), registration = Some(registration))
            .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
            .build()

          running(application) {
            val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(false).url)
            val result = route(application, request).value

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
          }
        }

        "the user is redirected when the incomplete prompt is shown" - {

          "to Check EU Details when one of the tax registered countries is incomplete" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers
              .set(TaxRegisteredInEuPage, true).success.value
              .set(EuCountryPage(Index(0)), country).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(AmendMode, Index(0)).url
            }
          }

          "to Tax Registered In EU when it has a 'yes' answer but all countries were removed" in {

            val answers = completeUserAnswers
              .set(TaxRegisteredInEuPage, true).success.value
              .set(EuCountryPage(Index(0)), country).success.value
              .remove(EuDetailsQuery(Index(0))).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER

              redirectLocation(result).value `mustBe` controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(AmendMode).url
            }
          }

          "to Previous Eu Vat Number when one of the previously registered countries is incomplete" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers
              .set(PreviouslyRegisteredPage, true).success.value
              .set(PreviousEuCountryPage(Index(0)), country).success.value
              .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
              .set(PreviousSchemeTypePage(Index(0), Index(0)), PreviousSchemeType.OSS).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe`
                controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(AmendMode, Index(0), Index(0)).url
            }
          }

          "to Has Trading Name when trading names are not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value
            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.routes.HasTradingNameController.onPageLoad(AmendMode).url
            }
          }

          "to Has Made Sales when eligible sales are not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.remove(HasMadeSalesPage).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.routes.HasMadeSalesController.onPageLoad(AmendMode).url
            }
          }

          "to Date of First Sale when date of first sale not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.routes.DateOfFirstSaleController.onPageLoad(AmendMode).url
            }
          }

          "to Has Website when websites are not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.set(HasWebsitePage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.routes.HasWebsiteController.onPageLoad(AmendMode).url
            }
          }

          "to Tax Registered In Eu when eu details are not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(AmendMode).url
            }
          }

          "to Previously Registered when previous registrations are not populated correctly" in {

            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))).toFuture

            val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers), registration = Some(registration))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .build()

            running(application) {
              val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) `mustBe` SEE_OTHER
              redirectLocation(result).value `mustBe` controllers.previousRegistrations.routes.PreviouslyRegisteredController.onPageLoad(AmendMode).url
            }
          }
        }
      }

      "when the submission fails because of a technical issue" - {

        "the user is redirected to the Error Submitting Registration page and their answers are saved" in {

          val errorResponse = UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")
          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Valid(registration).toFuture
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Left(errorResponse).toFuture
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, dataRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` amendRoutes.ErrorSubmittingAmendmentController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }

        "the user is redirected to the Journey Recovery page when saving answers fails" in {

          val errorResponse = UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")
          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Valid(registration).toFuture
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Left(errorResponse).toFuture
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(completeUserAnswers), registration = Some(registration))
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, amendRoutes.ChangeYourRegistrationController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, dataRequest)

            status(result) `mustBe` SEE_OTHER
            redirectLocation(result).value `mustBe` amendRoutes.ErrorSubmittingAmendmentController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }
    }
  }
}
