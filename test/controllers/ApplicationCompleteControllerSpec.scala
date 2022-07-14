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
import formats.Format.dateFormatter
import models.UserAnswers
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.inject.bind
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EmailConfirmationQuery
import services.DateService
import views.html.{ApplicationCompleteView, ApplicationCompleteWithEnrolmentView}

import java.time.{Clock, LocalDate, ZoneId}


class ApplicationCompleteControllerSpec extends SpecBase {

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
    )
  )

  "ApplicationComplete Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET with email confirmation and no enrolments" in {

        val emailAddress = "test@test.com"

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val dateService = application.injector.instanceOf[DateService]
          val commencementDate = LocalDate.now()
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate = dateService.isDOFSDifferentToCommencementDate(None, commencementDate)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET with email confirmation and enrolments enabled" in {

        val emailAddress = "test@test.com"

        val userAnswersWithEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail))
          .configure("features.enrolments-enabled" -> "true")
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteWithEnrolmentView]
          val dateService = application.injector.instanceOf[DateService]
          val commencementDate = LocalDate.now()
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate = dateService.isDOFSDifferentToCommencementDate(None, commencementDate)

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {

        val emailAddress = "test@test.com"

        val userAnswersWithoutEmail = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, false).success.value

        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutEmail))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val dateService = application.injector.instanceOf[DateService]
          val commencementDate = LocalDate.now()
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate = dateService.isDOFSDifferentToCommencementDate(None, commencementDate)
          val view = application.injector.instanceOf[ApplicationCompleteView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            false,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there is no Date Of First Sale and Is Planned First Eligible Sale is true" in {

        val emailAddress = "test@test.com"

        val answers = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application = applicationBuilder(userAnswers = Some(answers))
          .configure("features.enrolments-enabled" -> "false")
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val dateService = application.injector.instanceOf[DateService]
          val commencementDate = LocalDate.now()
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate = dateService.isDOFSDifferentToCommencementDate(None, commencementDate)

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is the same to the Commencement Date" in {

        val emailAddress = "test@test.com"

        val todayInstant = LocalDate.now().atStartOfDay(ZoneId.systemDefault).toInstant

        val stubClockForToday = Clock.fixed(todayInstant, ZoneId.systemDefault)

        val answers = userAnswers.copy()
          .set(DateOfFirstSalePage, LocalDate.now()).success.value
          .set(EmailConfirmationQuery, true).success.value

        val dateService = new DateService(stubClockForToday)

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .configure("features.enrolments-enabled" -> "false")
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val dateOfFirstSale = LocalDate.now()
          val commencementDate = LocalDate.now()
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate =
            dateService.isDOFSDifferentToCommencementDate(Some(dateOfFirstSale),commencementDate)

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when Date Of First Sale is different to the Commencement Date" in {

        val emailAddress = "test@test.com"

        val aug11thInstant =
          LocalDate.of(2021,8,11).atStartOfDay(ZoneId.systemDefault).toInstant

        val stubClockFor11Aug = Clock.fixed(aug11thInstant, ZoneId.systemDefault)

        val dateService = new DateService(stubClockFor11Aug)
        val answers = userAnswers.copy()
          .set(DateOfFirstSalePage, LocalDate.of(2021, 7, 1)).success.value
          .set(EmailConfirmationQuery, true).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .configure("features.enrolments-enabled" -> "false")
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val dateOfFirstSale = LocalDate.of(2021, 7, 1)
          val commencementDate = LocalDate.of(2021, 10, 1)
          val vatReturnEndDate = dateService.getVatReturnEndDate(commencementDate)
          val vatReturnDeadline = dateService.getVatReturnDeadline(vatReturnEndDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val startOfCurrentQuarter = dateService.startOfCurrentQuarter
          val startOfNextQuarter = dateService.startOfNextQuarter
          val isDOFSDifferentToCommencementDate =
            dateService.isDOFSDifferentToCommencementDate(Some(dateOfFirstSale),commencementDate)

          status(result) mustEqual OK

          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            vatReturnEndDate.format(dateFormatter),
            vatReturnDeadline.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            startOfCurrentQuarter.format(dateFormatter),
            startOfNextQuarter.format(dateFormatter),
            isDOFSDifferentToCommencementDate
          )(request, messages(application)).toString
        }
      }

      "must redirect to Journey Recovery and the correct view for a GET with no user answers" in {
        val application = applicationBuilder(userAnswers = Some(basicUserAnswers))
          .configure("features.enrolments-enabled" -> "false")
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
