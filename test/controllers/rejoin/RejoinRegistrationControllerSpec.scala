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

package controllers.rejoin

import base.SpecBase
import cats.data.NonEmptyChain
import cats.data.Validated.{Invalid, Valid}
import connectors.RegistrationConnector
import models.audit.{RegistrationAuditModel, RegistrationAuditType, SubmissionResult}
import models.requests.AuthenticatedDataRequest
import models.responses.UnexpectedResponseStatus
import models.{BusinessContactDetails, DataMissingError, Index, PreviousScheme, PreviousSchemeType, RejoinMode}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito
import org.mockito.Mockito.{doNothing, times, verify, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, EuTaxReferencePage, TaxRegisteredInEuPage}
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousSchemePage, PreviousSchemeTypePage, PreviouslyRegisteredPage}
import pages.{BusinessContactDetailsPage, HasMadeSalesPage, HasTradingNamePage, HasWebsitePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EuDetailsQuery
import repositories.AuthenticatedUserAnswersRepository
import services._
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier
import viewmodels.govuk.SummaryListFluency
import views.html.rejoin.RejoinRegistrationView

import java.time.LocalDate
import scala.concurrent.Future

class RejoinRegistrationControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, None, emptyUserAnswers)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, None, emptyUserAnswers)

  private val registration = RegistrationData.registration

  private val registrationValidationService = mock[RegistrationValidationService]
  private val registrationService = mock[RegistrationService]
  private val registrationConnector = mock[RegistrationConnector]
  private val rejoinRegistrationService = mock[RejoinRegistrationService]
  private val auditService = mock[AuditService]
  private val dateService = mock[DateService]
  private val country = arbitraryCountry.arbitrary.sample.value
  private val commencementDate = LocalDate.of(2022, 1, 1)

  override def beforeEach(): Unit = {
    Mockito.reset(registrationConnector)
    Mockito.reset(registrationValidationService)
    Mockito.reset(rejoinRegistrationService)
    Mockito.reset(auditService)
    Mockito.reset(dateService)
  }

  "RejoinRegistration Controller" - {

    "GET" - {

      "must redirect to Cannot rejoin if can rejoin is false" in {

        val commencementDate = LocalDate.of(2022, 1, 1)
        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
        when(dateService.startOfNextQuarter()) thenReturn commencementDate
        when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn false
        when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true


        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[DateService].toInstance(dateService))
          .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
          .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rejoin.routes.CannotRejoinController.onPageLoad().url
        }
      }

      "must return OK and the correct view for a GET" in {

        val commencementDate = LocalDate.of(2022, 1, 1)
        when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
        when(dateService.startOfNextQuarter()) thenReturn commencementDate
        when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
        when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true


        val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
          .overrides(bind[DateService].toInstance(dateService))
          .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
          .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RejoinRegistrationView]
          implicit val msgs: Messages = messages(application)
          val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(completeUserAnswers))
          val list = SummaryListViewModel(rows = getCYASummaryList(completeUserAnswers, dateService, registrationService, RejoinMode).futureValue)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = true, RejoinMode )(request, messages(application)).toString
        }
      }

      "must return OK and view with invalid prompt when" - {
        "trading name is missing" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode )(request, messages(application)).toString
          }
        }

        "websites are missing" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode )(request, messages(application)).toString
          }
        }

        "eligible sales is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode )(request, messages(application)).toString
          }
        }

        "tax registered in eu is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode)(request, messages(application)).toString
          }
        }

        "previous registrations is not populated correctly" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode)(request, messages(application)).toString
          }
        }

        "tax registered in eu has a country with missing data" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers
            .set(TaxRegisteredInEuPage, true).success.value
            .set(EuCountryPage(Index(0)), country).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode)(request, messages(application)).toString
          }
        }

        "previous registrations has a country with missing data" in {

          when(dateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(commencementDate)
          when(dateService.startOfNextQuarter()) thenReturn commencementDate
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          when(registrationService.eligibleSalesDifference(any(), any())) thenReturn true

          val answers = completeUserAnswers
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), country).success.value

          val application = applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
            .overrides(bind[RejoinRegistrationService].toInstance(rejoinRegistrationService))
            .build()

          running(application) {
            val request = FakeRequest(GET, controllers.rejoin.routes.RejoinRegistrationController.onPageLoad().url)
            val result = route(application, request).value
            val view = application.injector.instanceOf[RejoinRegistrationView]
            implicit val msgs: Messages = messages(application)
            val vatRegistrationDetailsList = SummaryListViewModel(rows = getCYAVatRegistrationDetailsSummaryList(answers))
            val list = SummaryListViewModel(rows = getCYASummaryList(answers, dateService, registrationService, RejoinMode).futureValue)

            status(result) mustEqual OK
            contentAsString(result) mustEqual view(vatRegistrationDetailsList, list, isValid = false, RejoinMode)(request, messages(application)).toString
          }
        }
      }
    }

    "on submit" - {

      "must audit the event and redirect to the next page and successfully" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
        when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
        when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
        when(registrationConnector.amendRegistration(any())(any())) thenReturn Future.successful(Right(()))
        when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
        doNothing().when(auditService).audit(any())(any(), any())

        val contactDetails = BusinessContactDetails("name", "0111 2223334", "email@example.com")
        val userAnswers = completeUserAnswers.set(BusinessContactDetailsPage, contactDetails).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(
            bind[RegistrationValidationService].toInstance(registrationValidationService),
            bind[RegistrationConnector].toInstance(registrationConnector),
            bind[RejoinRegistrationService].toInstance(rejoinRegistrationService),
            bind[AuditService].toInstance(auditService),
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          ).build()

        running(application) {
          val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(false).url)
          val result = route(application, request).value
          val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, userAnswers)
          val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Success, dataRequest)


          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual controllers.rejoin.routes.RejoinCompleteController.onPageLoad().url
          verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
        }
      }

      "when the submission fails because of a technical issue" - {

        "the user is redirected to the Error Submitting Rejoin Controller" in {

          val errorResponse = UnexpectedResponseStatus(INTERNAL_SERVER_ERROR, "foo")

          when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
          when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
          when(registrationConnector.amendRegistration(any())(any())) thenReturn Future.successful(Left(errorResponse))
          when(rejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
          doNothing().when(auditService).audit(any())(any(), any())

          val application = applicationBuilder(userAnswers = Some(completeUserAnswers))
            .overrides(
              bind[RegistrationValidationService].toInstance(registrationValidationService),
              bind[RegistrationConnector].toInstance(registrationConnector),
              bind[RejoinRegistrationService].toInstance(rejoinRegistrationService),
              bind[AuditService].toInstance(auditService)
            ).build()

          running(application) {
            val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(false).url)
            val result = route(application, request).value
            val dataRequest = AuthenticatedDataRequest(request, testCredentials, vrn, None, basicUserAnswersWithVatInfo)
            val expectedAuditEvent = RegistrationAuditModel.build(RegistrationAuditType.AmendRegistration, registration, SubmissionResult.Failure, dataRequest)


            status(result) mustEqual SEE_OTHER
            redirectLocation(result).value mustEqual controllers.rejoin.routes.ErrorSubmittingRejoinController.onPageLoad().url
            verify(auditService, times(1)).audit(eqTo(expectedAuditEvent))(any(), any())
          }
        }
      }

      "when the user has not answered all necessary data" - {

        "the user is redirected when the incomplete prompt is shown" - {

          "to Check EU Details when one of the tax registered countries is incomplete" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers
              .set(TaxRegisteredInEuPage, true).success.value
              .set(EuCountryPage(Index(0)), country).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.euDetails.routes.CheckEuDetailsAnswersController.onPageLoad(RejoinMode, Index(0)).url

            }

          }

          "to Tax Registered In EU when it has a 'yes' answer but all countries were removed" in {
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers
              .set(TaxRegisteredInEuPage, true).success.value
              .set(EuCountryPage(Index(0)), country).success.value
              .remove(EuDetailsQuery(Index(0))).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(RejoinMode).url

            }
          }

          "to Previous Eu Vat Number when one of the previously registered countries is incomplete" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers
              .set(PreviouslyRegisteredPage, true).success.value
              .set(PreviousEuCountryPage(Index(0)), country).success.value
              .set(PreviousSchemePage(Index(0), Index(0)), PreviousScheme.OSSU).success.value
              .set(PreviousSchemeTypePage(Index(0), Index(0)), PreviousSchemeType.OSS).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual
                controllers.previousRegistrations.routes.PreviousOssNumberController.onPageLoad(RejoinMode, Index(0), Index(0)).url

            }
          }

          "to Has Trading Name when trading names are not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers.set(HasTradingNamePage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasTradingNameController.onPageLoad(RejoinMode).url

            }
          }

          "to Has Made Sales when eligible sales are not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
            val answers = completeUserAnswers.remove(HasMadeSalesPage).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasMadeSalesController.onPageLoad(RejoinMode).url

            }
          }

          "to Date of First Sale when date of first sale is not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
            val answers = completeUserAnswers.set(HasMadeSalesPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.DateOfFirstSaleController.onPageLoad(RejoinMode).url

            }
          }

          "to Has Website when websites are not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers.set(HasWebsitePage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.routes.HasWebsiteController.onPageLoad(RejoinMode).url

            }
          }

          "to Tax Registered In Eu when eu details are not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers.set(TaxRegisteredInEuPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.euDetails.routes.TaxRegisteredInEuController.onPageLoad(RejoinMode).url

            }
          }


          "to Previously Registered when previous registrations are not populated correctly" in {
            when(registrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn
              Future.successful(Invalid(NonEmptyChain(DataMissingError(EuTaxReferencePage(Index(0))))))
            when(registrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))

            val answers = completeUserAnswers.set(PreviouslyRegisteredPage, true).success.value

            val application = applicationBuilder(userAnswers = Some(answers))
              .overrides(bind[RegistrationValidationService].toInstance(registrationValidationService))
              .overrides(bind[RegistrationConnector].toInstance(registrationConnector))
              .build()

            running(application) {
              val request = FakeRequest(POST, controllers.rejoin.routes.RejoinRegistrationController.onSubmit(true).url)
              val result = route(application, request).value

              status(result) mustEqual SEE_OTHER
              redirectLocation(result).value mustEqual controllers.previousRegistrations.routes.AddPreviousRegistrationController.onPageLoad(RejoinMode).url

            }
          }
        }
      }

    }
  }
}
