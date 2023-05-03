/*
 * Copyright 2023 HM Revenue & Customs
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
import models.{Period, UserAnswers}
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.mvc.AnyContent
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EmailConfirmationQuery
import services.{CoreRegistrationValidationService, DateService, PeriodService}
import uk.gov.hmrc.http.HeaderCarrier
import views.html.{ApplicationCompleteView, ApplicationCompleteWithEnrolmentView}

import java.time.{Clock, LocalDate, ZoneId}
import scala.concurrent.{ExecutionContext, Future}


class ApplicationCompleteControllerSpec extends SpecBase with MockitoSugar {

  private val periodService = mock[PeriodService]
  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockDateService = mock[DateService]

  private implicit val hc: HeaderCarrier = HeaderCarrier()
  private implicit val ec: ExecutionContext = ExecutionContext.Implicits.global
  private val request = AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, emptyUserAnswers)
  private implicit val dataRequest: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest(request, testCredentials, vrn, emptyUserAnswers)

  private  val userAnswers = UserAnswers(
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

  "ApplicationComplete Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET with no enrolments" in {

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.enrolments-enabled" -> "false")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(arbitraryStartDate)
        when(mockDateService.startOfNextQuarter) thenReturn arbitraryStartDate
        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val commencementDate = mockDateService.calculateCommencementDate(userAnswersWithEmail).futureValue
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET with enrolments enabled" in {

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.enrolments-enabled" -> "true")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.now(stubClockAtArbitraryDate))

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

        running(application) {
          implicit val msgs: Messages = messages(application)
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteWithEnrolmentView]
          val commencementDate = LocalDate.now(stubClockAtArbitraryDate)
          val periodOfFirstReturn = periodService.getFirstReturnPeriod(commencementDate)
          val nextPeriod = periodService.getNextPeriod(periodOfFirstReturn)
          val firstDayOfNextPeriod = nextPeriod.firstDay

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {

        val userAnswersWithoutEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
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
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.now(stubClockAtArbitraryDate))

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

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

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there is no Date Of First Sale and Is Planned First Eligible Sale is true" in {

        val answers = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .configure("features.enrolments-enabled" -> "false")
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
          .overrides(bind[PeriodService].toInstance(periodService))
          .overrides(bind[DateService].toInstance(mockDateService))
          .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.now(stubClockAtArbitraryDate))
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

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
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
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
            .build()

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.now(stubClockForToday))

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

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

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is different to the Commencement Date" in {

        when(periodService.getFirstReturnPeriod(any())) thenReturn Period(2022, Q4)
        when(periodService.getNextPeriod(any())) thenReturn Period(2023, Q1)
        when(mockDateService.calculateCommencementDate(any())(any(), any(), any())) thenReturn Future.successful(LocalDate.of(2021, 10, 1))

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Future.successful(None)

        when(mockRegistrationConnector.getSavedExternalEntry()(any())) thenReturn Future.successful(Right(ExternalEntryUrl(None)))

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

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            "",
            vrn,
            showEmailConfirmation = false,
            config.feedbackUrl(request),
            commencementDate.format(dateFormatter),
            None,
            "Company name",
            periodOfFirstReturn.displayShortText,
            firstDayOfNextPeriod.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery and the correct view for a GET with no user answers" in {

        val application = applicationBuilder(userAnswers = None)
          .configure("features.enrolments-enabled" -> "false")
          .overrides(bind[PeriodService].toInstance(periodService))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val result = route(application, request).value
          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
        }
      }
    }
  }
}
