/*
 * Copyright 2021 HM Revenue & Customs
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
import config.{Constants, FrontendAppConfig}
import formats.Format.dateFormatter
import models.UserAnswers
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EmailConfirmationQuery
import services.DateService
import views.html.ApplicationCompleteView

import java.time.{Clock, Instant, LocalDate, ZoneId}

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
      DateOfFirstSalePage.toString -> Json.toJson(arbitraryDate)
    )
  )

  "ApplicationComplete Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET with email confirmation" in {
        val emailAddress = "test@test.com"
        val userAnswersWithEmail = userAnswers.copy().set(EmailConfirmationQuery, true).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswersWithEmail)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val dateService = application.injector.instanceOf[DateService]
          val startDate = dateService.startDateBasedOnFirstSale(arbitraryDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            startDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {
        val emailAddress = "test@test.com"
        val userAnswersWithoutEmail = userAnswers.copy().set(EmailConfirmationQuery, false).success.value
        val application = applicationBuilder(userAnswers = Some(userAnswersWithoutEmail)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val dateService = application.injector.instanceOf[DateService]
          val startDate = dateService.startDateBasedOnFirstSale(arbitraryDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            false,
            startDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }
    }

    "when the scheme has not started" - {

      val date = datesBetween(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 30)).sample.value
      val instant: Instant = date.atStartOfDay(ZoneId.systemDefault).toInstant
      val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

      "must return OK and the correct view for a GET with email confirmation" in {
        val emailAddress = "test@test.com"
        val userAnswersWithEmail = userAnswers.copy().set(EmailConfirmationQuery, true).success.value
        val application = applicationBuilder(Some(userAnswersWithEmail), Some(stubClock)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val view = application.injector.instanceOf[ApplicationCompleteView]
          val dateService = application.injector.instanceOf[DateService]
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            Constants.schemeStartDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET without email confirmation" in {
        val emailAddress = "test@test.com"
        val userAnswersWithoutEmail = userAnswers.copy().set(EmailConfirmationQuery, false).success.value
        val application = applicationBuilder(Some(userAnswersWithoutEmail), Some(stubClock)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val dateService = application.injector.instanceOf[DateService]
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            false,
            Constants.schemeStartDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }
    }
  }
}
