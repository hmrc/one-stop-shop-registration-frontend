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
import config.FrontendAppConfig
import formats.Format.dateFormatter
import models.UserAnswers
import pages.{BusinessContactDetailsPage, DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EmailConfirmationQuery
import services.DateService
import views.html.ApplicationCompleteView

import java.time.LocalDate


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
          val commencementDate = dateService.startDateBasedOnFirstSale(arbitraryDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
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
          val commencementDate = dateService.startDateBasedOnFirstSale(arbitraryDate)
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            false,
            commencementDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }

      "must return OK and the correct view when there is no Date Of First Sale and Is Planned First Eligible Sale is true" in {

        val emailAddress = "test@test.com"
        val answer = userAnswers.copy()
          .remove(DateOfFirstSalePage).success.value
          .set(HasMadeSalesPage, false).success.value
          .set(IsPlanningFirstEligibleSalePage, true).success.value
          .set(EmailConfirmationQuery, true).success.value
        val application = applicationBuilder(userAnswers = Some(answer)).build()

        running(application) {
          val request = FakeRequest(GET, routes.ApplicationCompleteController.onPageLoad().url)
          val config = application.injector.instanceOf[FrontendAppConfig]
          val result = route(application, request).value
          val dateService = application.injector.instanceOf[DateService]
          val commencementDate = LocalDate.now()
          val lastDayOfCalendarQuarter = dateService.lastDayOfCalendarQuarter
          val lastDayOfMonthAfterCalendarQuarter = dateService.lastDayOfMonthAfterCalendarQuarter

          val view = application.injector.instanceOf[ApplicationCompleteView]
          status(result) mustEqual OK
          contentAsString(result) mustEqual view(
            emailAddress,
            vrn,
            config.feedbackUrl(request),
            true,
            commencementDate.format(dateFormatter),
            lastDayOfCalendarQuarter.format(dateFormatter),
            lastDayOfMonthAfterCalendarQuarter.format(dateFormatter)
          )(request, messages(application)).toString
        }
      }
    }
  }
}
