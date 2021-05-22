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

package forms

import forms.behaviours.{DateBehaviours, OptionFieldBehaviours}
import models.StartDateOption.{EarlierDate, NextPeriod}
import models.{StartDate, StartDateOption}
import org.scalacheck.Gen
import play.api.data.{Form, FormError}
import services.StartDateService

import java.time.format.DateTimeFormatter
import java.time.{Clock, LocalDate, ZoneId}

class StartDateFormProviderSpec extends OptionFieldBehaviours with DateBehaviours {

  private val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

  def getForm(today: LocalDate): Form[StartDate] = {
    val stubClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
    val startDateService = new StartDateService(stubClock)

    new StartDateFormProvider(stubClock, startDateService)()
  }

  "form" - {

    "when NextPeriod is selected" - {

      "must bind" in {
        val arbitraryDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
        val stubClock = Clock.fixed(arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
        val startDateService = new StartDateService(stubClock)

        val form = new StartDateFormProvider(stubClock, startDateService)()

        val result = form.bind(Map("choice" -> NextPeriod.toString))
        result.value.value mustEqual StartDate(NextPeriod, None)
        result.errors mustBe empty
      }
    }

    "when EarlierDate is selected" - {

      "must not bind if no date is supplied" in {
        val arbitraryDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
        val stubClock = Clock.fixed(arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
        val startDateService = new StartDateService(stubClock)

        val form = new StartDateFormProvider(stubClock, startDateService)()

        val result = form.bind(Map("choice" -> EarlierDate.toString))
        result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.allRequired")
      }

      "on the 1st to the 10th of any month" - {

        val dateGen = for {
          day <- Gen.choose(1, 10)
          date <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
        } yield date.withDayOfMonth(day)

        val today = dateGen.sample.value
        val stubClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
        val startDateService = new StartDateService(stubClock)

        val form = new StartDateFormProvider(stubClock, startDateService)()

        val validDates = datesBetween(today.minusMonths(1).withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth))

        "must bind valid data" in {

          forAll(validDates -> "valid date") {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.value.value mustEqual StartDate(EarlierDate, Some(date))
              result.errors mustBe empty
          }
        }

        "must fail to bind a date after the end of this month" in {

          val datesAfterThisMonth = datesBetween(today.withDayOfMonth(today.lengthOfMonth).plusDays(1), today.plusYears(10))

          forAll(datesAfterThisMonth) {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError(
                "earlierDate",
                "startDate.earlierDate.error.maxDate",
                Seq(startDateService.latestAlternativeDate.format(dateFormatter))
              )
          }
        }

        "must fail to bind a date earlier than the first of the previous month" in {

          val datesBeforeLastMonth = datesBetween(today.minusYears(10), today.minusMonths(1).withDayOfMonth(1).minusDays(1))

          forAll(datesBeforeLastMonth) {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError(
                "earlierDate",
                "startDate.earlierDate.error.minDate",
                Seq(startDateService.earliestAlternativeDate.format(dateFormatter))
              )
          }
        }
      }

      "after the 10th of any month" - {

        val dateGen = for {
          date <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
          dayOfMonth <- Gen.choose(11, date.lengthOfMonth)
        } yield date.withDayOfMonth(dayOfMonth)

        val today = dateGen.sample.value
        val stubClock = Clock.fixed(today.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
        val startDateService = new StartDateService(stubClock)

        val form = new StartDateFormProvider(stubClock, startDateService)()

        val validDates = datesBetween(today.withDayOfMonth(1), today.withDayOfMonth(today.lengthOfMonth))

        "must bind valid data" in {

          forAll(validDates -> "valid date") {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.value.value mustEqual StartDate(EarlierDate, Some(date))
              result.errors mustBe empty
          }
        }

        "must fail to bind a date after the end of this month" in {

          val generator = datesBetween(today.withDayOfMonth(today.lengthOfMonth).plusDays(1), today.plusYears(10))

          forAll(generator -> "invalid dates") {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError(
                "earlierDate",
                "startDate.earlierDate.error.maxDate",
                Seq(startDateService.latestAlternativeDate.format(dateFormatter))
              )
          }
        }

        "must fail to bind a date earlier than the first of this month" in {

          val generator = datesBetween(today.minusYears(10), today.withDayOfMonth(1).minusDays(1))

          forAll(generator -> "invalid dates") {
            date =>

              val data = Map(
                "choice"            -> StartDateOption.EarlierDate.toString,
                "earlierDate.day"   -> date.getDayOfMonth.toString,
                "earlierDate.month" -> date.getMonthValue.toString,
                "earlierDate.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError(
                "earlierDate",
                "startDate.earlierDate.error.minDate",
                Seq(startDateService.earliestAlternativeDate.format(dateFormatter))
              )
          }
        }
      }
    }

    "must not bind" - {

      val arbitraryDate = datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31)).sample.value
      val stubClock = Clock.fixed(arbitraryDate.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)
      val startDateService = new StartDateService(stubClock)

      val form = new StartDateFormProvider(stubClock, startDateService)()

      "when no choice is selected" in {
        val result = form.bind(Map.empty[String, String])
        result.errors must contain only FormError("choice", "startDate.choice.error.required")
      }

      "when EarlierDate is selected" - {

        "and no day is provided" in {

          val data = Map(
            "choice"            -> StartDateOption.EarlierDate.toString,
            "earlierDate.month" -> arbitraryDate.getMonthValue.toString,
            "earlierDate.year"  -> arbitraryDate.getYear.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.required", Seq("day"))
        }

        "and no month is provided" in {

          val data = Map(
            "choice"            -> StartDateOption.EarlierDate.toString,
            "earlierDate.day" -> arbitraryDate.getDayOfMonth.toString,
            "earlierDate.year"  -> arbitraryDate.getYear.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.required", Seq("month"))
        }

        "and no year is provided" in {

          val data = Map(
            "choice"            -> StartDateOption.EarlierDate.toString,
            "earlierDate.day" -> arbitraryDate.getDayOfMonth.toString,
            "earlierDate.month"  -> arbitraryDate.getMonthValue.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.required", Seq("year"))
        }

        "and no day or month is provided" in {

          val data = Map(
            "choice"           -> StartDateOption.EarlierDate.toString,
            "earlierDate.year" -> arbitraryDate.getYear.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.twoRequired", Seq("day", "month"))
        }

        "and no day or year is provided" in {

          val data = Map(
            "choice"            -> StartDateOption.EarlierDate.toString,
            "earlierDate.month" -> arbitraryDate.getMonthValue.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.twoRequired", Seq("day", "year"))
        }

        "and no month or year is provided" in {

          val data = Map(
            "choice"          -> StartDateOption.EarlierDate.toString,
            "earlierDate.day" -> arbitraryDate.getDayOfMonth.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.twoRequired", Seq("month", "year"))
        }

        "and an invalid date is provided" in {

          val data = Map(
            "choice"            -> StartDateOption.EarlierDate.toString,
            "earlierDate.day"   -> "32",
            "earlierDate.month" -> arbitraryDate.getMonthValue.toString,
            "earlierDate.year"  -> arbitraryDate.getYear.toString
          )

          val result = form.bind(data)
          result.errors must contain only FormError("earlierDate", "startDate.earlierDate.error.invalid")
        }
      }
    }
  }
}