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
import config.FrontendAppConfig
import connectors.RegistrationConnector
import controllers.amend.routes as amendRoutes
import models.Quarter.{Q1, Q4}
import models.external.ExternalEntryUrl
import models.requests.AuthenticatedDataRequest
import models.{Period, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.{EmailConfirmationQuery, OriginalRegistrationQuery}
import services.{CoreRegistrationValidationService, DateService, PeriodService, RegistrationService}
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps
import viewmodels.govuk.all.SummaryListViewModel
import views.html.amend.AmendCompleteView

import java.time.{Clock, LocalDate, ZoneId}

class AmendCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val periodService = mock[PeriodService]
  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockDateService = mock[DateService]

  private val mockRegistration = RegistrationData.registration
  private val mockRegistrationService = mock[RegistrationService]
  private implicit val hc: HeaderCarrier = HeaderCarrier()
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

  "AmendComplete Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET with enrolments enabled" in {

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value
          .set(OriginalRegistrationQuery, mockRegistration).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
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
        when(mockRegistrationService.eligibleSalesDifference(any(), any())) thenReturn true

        running(application) {
          val request = FakeRequest(GET, amendRoutes.AmendCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[AmendCompleteView]
          implicit val msgs: Messages = messages(application)
          val summaryList = SummaryListViewModel(rows = getAmendedCYASummaryList(
            userAnswersWithEmail,
            mockDateService,
            mockRegistrationService,
            Some(mockRegistration)).futureValue)

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            vrn,
            config.feedbackUrl(request),
            None,
            yourAccountUrl,
            "Company name",
            summaryList
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {

        val userAnswersWithoutEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, false).success.value
          .set(OriginalRegistrationQuery, mockRegistration).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutEmail))
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
          val request = FakeRequest(GET, amendRoutes.AmendCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[AmendCompleteView]
          implicit val msgs: Messages = messages(application)
          val summaryList = SummaryListViewModel(rows = getAmendedCYASummaryList(
            userAnswersWithoutEmail,
            mockDateService,
            mockRegistrationService,
            Some(mockRegistration)).futureValue)

          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            vrn,
            config.feedbackUrl(request),
            None,
            yourAccountUrl,
            "Company name",
            summaryList
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there is no Date Of First Sale and Is Planned First Eligible Sale is true" in {

        val answers = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value
          .set(OriginalRegistrationQuery, mockRegistration).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
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
          val request = FakeRequest(GET, amendRoutes.AmendCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          implicit val msgs: Messages = messages(application)
          val summaryList = SummaryListViewModel(rows = getAmendedCYASummaryList(
            answers,
            mockDateService,
            mockRegistrationService,
            Some(mockRegistration)).futureValue)

          val view = application.injector.instanceOf[AmendCompleteView]
          status(result) `mustBe` OK
          contentAsString(result) `mustBe` view(
            vrn,
            config.feedbackUrl(request),
            None,
            yourAccountUrl,
            "Company name",
            summaryList
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is the same as the Commencement Date" in {

        val todayInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault).toInstant

        val stubClockForToday = Clock.fixed(todayInstant, ZoneId.systemDefault)

        val answers = userAnswers.copy()
          .set(DateOfFirstSalePage, LocalDate.now()).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value
          .set(OriginalRegistrationQuery, mockRegistration).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[PeriodService].toInstance(periodService))
            .overrides(bind[DateService].toInstance(mockDateService))
            .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Some(LocalDate.now(stubClockForToday)).toFuture

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Right(ExternalEntryUrl(None)).toFuture

        running(application) {
          val request = FakeRequest(GET, amendRoutes.AmendCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[AmendCompleteView]
          implicit val msgs: Messages = messages(application)
          val summaryList = SummaryListViewModel(rows = getAmendedCYASummaryList(
            answers,
            mockDateService,
            mockRegistrationService,
            Some(mockRegistration)).futureValue)

          status(result) `mustBe` OK

          contentAsString(result) `mustBe` view(
            vrn,
            config.feedbackUrl(request),
            None,
            yourAccountUrl,
            "Company name",
            summaryList
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
          .set(HasMadeSalesPage, false).success.value
          .set(EmailConfirmationQuery, true).success.value
          .set(OriginalRegistrationQuery, mockRegistration).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
            .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
            .overrides(bind[PeriodService].toInstance(periodService))
            .overrides(bind[DateService].toInstance(mockDateService))
            .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
            .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.AmendCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[AmendCompleteView]
          implicit val msgs: Messages = messages(application)
          val summaryList = SummaryListViewModel(rows = getAmendedCYASummaryList(
            answers,
            mockDateService,
            mockRegistrationService,
            Some(mockRegistration)).futureValue)

          status(result) `mustBe` OK

          contentAsString(result) `mustBe` view(
            vrn,
            config.feedbackUrl(request),
            None,
            yourAccountUrl,
            "Company name",
            summaryList
          )(request, messages(application)).toString
        }
      }
    }
  }
}
