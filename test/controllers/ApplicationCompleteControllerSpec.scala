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
import formats.Format.dateFormatter
import models.Quarter.{Q1, Q4}
import models.external.ExternalEntryUrl
import models.iossRegistration.IossEtmpDisplayRegistration
import models.requests.AuthenticatedDataRequest
import models.{BankDetails, BusinessContactDetails, Period, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.*
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{AllTradingNames, EmailConfirmationQuery}
import services.{CoreRegistrationValidationService, DateService, PeriodService}
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps
import views.html.ApplicationCompleteView

import java.time.{Clock, LocalDate, ZoneId}
import scala.concurrent.ExecutionContext

class ApplicationCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val periodService = mock[PeriodService]
  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockDateService = mock[DateService]

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, None, emptyUserAnswers, None, 0, None)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, None, emptyUserAnswers, None, 0, None)

  private val userAnswers = UserAnswers(
    userAnswersId,
    Json.obj(
      BusinessContactDetailsPage.toString -> Json.obj(
        "fullName" -> "value 1",
        "telephoneNumber" -> "value 2",
        "emailAddress" -> "test@test.com",
        "websiteAddress" -> "value 4",
      ),
      DateOfFirstSalePage.toString -> Json.toJson(arbitraryStartDate)
    ),
    vatInfo = Some(vatCustomerInfo)
  )

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  private val iossBusinessContactDetails: BusinessContactDetails = BusinessContactDetails(
    fullName = iossEtmpDisplayRegistration.schemeDetails.contactName,
    telephoneNumber = iossEtmpDisplayRegistration.schemeDetails.businessTelephoneNumber,
    emailAddress = iossEtmpDisplayRegistration.schemeDetails.businessEmailId
  )

  private val iossBankDetails: BankDetails = BankDetails(
    accountName = iossEtmpDisplayRegistration.bankDetails.accountName,
    bic = iossEtmpDisplayRegistration.bankDetails.bic,
    iban = iossEtmpDisplayRegistration.bankDetails.iban
  )

  "ApplicationComplete Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET with no enrolments" in {

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.registration.email-enabled" -> "true")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(arbitraryStartDate).toFuture
        when(mockDateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(userAnswersWithEmail).futureValue.get
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET with enrolments enabled" in {

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.enrolments-enabled" -> "true")
          .configure("features.registration.email-enabled" -> "true")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.now(stubClockAtArbitraryDate)).toFuture

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = LocalDate.now(stubClockAtArbitraryDate)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {

        val userAnswersWithoutEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutEmail))
          .configure("features.enrolments-enabled" -> "false")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.now(stubClockAtArbitraryDate)).toFuture

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val commencementDate = LocalDate.now(stubClockAtArbitraryDate)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay
          val view = application.injector.instanceOf[ApplicationCompleteView]

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there is no Date Of First Sale and Is Planned First Eligible Sale is true" in {

        val answers = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .configure("features.enrolments-enabled" -> "false")
          .configure("features.registration.email-enabled" -> "true")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.now(stubClockAtArbitraryDate)).toFuture
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val commencementDate = LocalDate.now(stubClockAtArbitraryDate)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is the same as the Commencement Date" in {

        val todayInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault).toInstant

        val stubClockForToday = Clock.fixed(todayInstant, ZoneId.systemDefault)

        val answers = userAnswers.copy()
          .set(DateOfFirstSalePage, LocalDate.now()).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[PeriodService].toInstance(periodService))
            .overrides(bind[DateService].toInstance(mockDateService))
            .configure("features.enrolments-enabled" -> "false")
            .configure("features.registration.email-enabled" -> "true")
            .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.now(stubClockForToday)).toFuture

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = LocalDate.now()
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK

          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is different to the Commencement Date" in {

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.of(2021, 10, 1)).toFuture

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        val answers = userAnswers.copy()
          .set(DateOfFirstSalePage, LocalDate.of(2021, 7, 1)).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[PeriodService].toInstance(periodService))
            .overrides(bind[DateService].toInstance(mockDateService))
            .configure("features.enrolments-enabled" -> "false")
            .configure("features.registration.email-enabled" -> "true")
            .build()

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = LocalDate.of(2021, 10, 1)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK

          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            None,
            hasUpdatedIossRegistration = false,
            0
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when an IOSS Registration is present and no user answers have been updated" in {

        val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
          iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

        val updatedAnswers = userAnswers
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, nonExcludedIossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value
          .set(BusinessContactDetailsPage, iossBusinessContactDetails).success.value
          .set(BankDetailsPage, iossBankDetails).success.value

        val application = applicationBuilder(
          userAnswers = Some(updatedAnswers),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
          numberOfIossRegistrations = 1
        )
          .configure("features.registration.email-enabled" -> false)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(arbitraryStartDate).toFuture
        when(mockDateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(updatedAnswers).futureValue.get
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            Some(nonExcludedIossEtmpDisplayRegistration),
            hasUpdatedIossRegistration = false,
            1
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when an IOSS Registration is present and some user answers have been updated" in {

        val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
          iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

        val updatedAnswers = userAnswers
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, nonExcludedIossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value
          .set(BusinessContactDetailsPage, iossBusinessContactDetails).success.value
          .set(BankDetailsPage, iossBankDetails.copy(accountName = "Test account name")).success.value

        val application = applicationBuilder(
          userAnswers = Some(updatedAnswers),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
          numberOfIossRegistrations = 1
        )
          .configure("features.registration.email-enabled" -> false)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(arbitraryStartDate).toFuture
        when(mockDateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(updatedAnswers).futureValue.get
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            Some(nonExcludedIossEtmpDisplayRegistration),
            hasUpdatedIossRegistration = true,
            1
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when an excluded IOSS Registration is present and some user answers have been updated" in {

        val updatedAnswers = userAnswers
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value
          .set(BusinessContactDetailsPage, iossBusinessContactDetails.copy(telephoneNumber = "123456789")).success.value
          .set(BankDetailsPage, iossBankDetails).success.value

        val application = applicationBuilder(
          userAnswers = Some(updatedAnswers),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration),
          numberOfIossRegistrations = 1
        )
          .configure("features.registration.email-enabled" -> false)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(arbitraryStartDate).toFuture
        when(mockDateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(updatedAnswers).futureValue.get
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            Some(iossEtmpDisplayRegistration),
            hasUpdatedIossRegistration = true,
            1
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when multiple IOSS Registrations are present and some user answers have been updated" in {

        val nonExcludedIossEtmpDisplayRegistration: IossEtmpDisplayRegistration =
          iossEtmpDisplayRegistration.copy(exclusions = Seq.empty)

        val updatedAnswers = userAnswers
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, nonExcludedIossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value
          .set(BusinessContactDetailsPage, iossBusinessContactDetails.copy(fullName = "Test name")).success.value
          .set(BankDetailsPage, iossBankDetails).success.value

        val application = applicationBuilder(
          userAnswers = Some(updatedAnswers),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(nonExcludedIossEtmpDisplayRegistration),
          numberOfIossRegistrations = 2
        )
          .configure("features.registration.email-enabled" -> false)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(arbitraryStartDate).toFuture
        when(mockDateService.startOfNextQuarter()) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(updatedAnswers).futureValue.get
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            "test@test.com",
            vrn,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter),
            Some(nonExcludedIossEtmpDisplayRegistration),
            hasUpdatedIossRegistration = true,
            2
          )(request, messages(application)).toString
        }
      }
    }
  }
}
