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

package services

import base.SpecBase
import generators.Generators
import org.scalacheck.Gen
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks

import java.time.Month._
import java.time.{Clock, LocalDate, Year, ZoneId}

class DateServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def getStubClock(date: LocalDate): Clock =
    Clock.fixed(date.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)

  "getRegistrationDate" - {
    "must return LocalDate.now" in {
      val stubClock = getStubClock(LocalDate.now())
      val service   = new DateService(stubClock)
    }
  }

//  "shouldStartDateBeInCurrentQuarter" - {

//    "when user registers AFTER 10th of month" - {
//
//      val registrationDate = LocalDate.now().withDayOfMonth(11)
//
//      "must return true when first sale BETWEEN 1st of same month and registration date" in {
//        val firstEligibleSaleDate = LocalDate.now().withDayOfMonth(1)
//
//        val stubClock = getStubClock(LocalDate.now())
//        val dateService = new DateService(stubClock)
//
//        dateService.shouldStartDateBeInCurrentQuarter(registrationDate, firstEligibleSaleDate) mustBe true
//      }
//
//      "must return false when first sale BEFORE 01st of same month" in {
//        val firstEligibleSaleDate = registrationDate.minusMonths(1)
//
//        val stubClock = getStubClock(LocalDate.now())
//        val dateService = new DateService(stubClock)
//
//        dateService.shouldStartDateBeInCurrentQuarter(registrationDate, firstEligibleSaleDate) mustBe false
//      }
//    }

//    "when user registers BEFORE 11th of month" - {
//
//      val registrationDate = LocalDate.now().withDayOfMonth(1)
//
//      "must return true when first sale AFTER 1st of previous month" in {
//        val firstEligibleSaleDate = registrationDate.minusDays(1)
//
//        val stubClock = getStubClock(LocalDate.now())
//        val dateService = new DateService(stubClock)
//
//        dateService.shouldStartDateBeInCurrentQuarter(registrationDate, firstEligibleSaleDate) mustBe true
//      }
//
//      "must return false when first sale BEFORE 1st of previous month" in {
//        val firstEligibleSaleDate = registrationDate.minusMonths(2)
//
//        val stubClock = getStubClock(LocalDate.now())
//        val dateService = new DateService(stubClock)
//
//        dateService.shouldStartDateBeInCurrentQuarter(registrationDate, firstEligibleSaleDate) mustBe false
//      }
//    }
//  }

  ".startOfNextQuarter" - {

    "must be 1st January of the next year for any date in October, November or December" in {

      forAll(datesBetween(LocalDate.of(2021, 10, 1), LocalDate.of(2021, 12, 31))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.startOfNextQuarter mustEqual LocalDate.of(2022, 1, 1)
      }
    }

    "must be 1st April for any date in January, February or March" in {

      forAll(datesBetween(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.startOfNextQuarter mustEqual LocalDate.of(2022, 4, 1)
      }
    }

    "must be 1st July for any date in April, May or June" in {

      forAll(datesBetween(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 6, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.startOfNextQuarter mustEqual LocalDate.of(2022, 7, 1)
      }
    }

    "must be 1st October for any date in July, August or September" in {

      forAll(datesBetween(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.startOfNextQuarter mustEqual LocalDate.of(2022, 10, 1)
      }
    }
  }

  ".lastDayOfCalendarQuarter" - {

    "must be 30th September for any date in July, August or September" in {

      forAll(datesBetween(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.lastDayOfCalendarQuarter mustEqual LocalDate.of(2022, 9, 30)
      }
    }
  }

  ".lastDayOfMonthAfterCalendarQuarter" - {

    "must be 31st October for any date in July, August or September" in {

      forAll(datesBetween(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new DateService(stubClock)

          service.lastDayOfMonthAfterCalendarQuarter mustEqual LocalDate.of(2022, 10, 31)
      }
    }
  }

  ".isStartDateAfterThe10th" - {

    "must be true if start date is after the 10th of the month" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          dateAfterThe10th <- datesBetween(
            LocalDate.of(2021, 7, 11),
            LocalDate.of(2021, 7, 31)
          )
          dayOfToday <- Gen.choose(11, 31)
        } yield (
          dateAfterThe10th,
          dateAfterThe10th.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (dateAfterThe10th, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateAfterThe10th(dateAfterThe10th) mustEqual true
      }
    }

    "must be false if start date is before the 11th of the month" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          dateBeforeThe11th <- datesBetween(
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 7, 10)
          )
          dayOfToday <- Gen.choose(1, 30)
        } yield (
            dateBeforeThe11th,
            dateBeforeThe11th.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (dateBeforeThe11th, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateAfterThe10th(dateBeforeThe11th) mustEqual false
      }
    }
  }

  ".isStartDateInFirstQuarter" - {

    "must be true if start date is within 1 July and 30 September" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          startDate  <- datesBetween(
            LocalDate.of(2021, 7, 1),
            LocalDate.of(2021, 9, 30)
          )
          dayOfToday <- Gen.choose(1, 30)
        } yield (
            startDate,
            startDate.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (startDate, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateInFirstQuarter(startDate) mustEqual true
      }
    }

    "must be false if start date is NOT within 1 July and 30 September" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          dateOutsideFirstQuarter <- datesBetween(
            LocalDate.of(2021, 10, 1),
            LocalDate.of(2021, 12, 31)
          )
          dayOfToday <- Gen.choose(1, 30)
        } yield (
            dateOutsideFirstQuarter,
            dateOutsideFirstQuarter.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (dateOutsideFirstQuarter, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateInFirstQuarter(dateOutsideFirstQuarter) mustEqual false
      }
    }
  }

  ".isStartDateAfterFirstQuarter" - {

    "must be true if start date is after 30 September" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          afterFirstQuarterDate  <- datesBetween(
            LocalDate.of(2021, 10, 1),
            LocalDate.of(2021, 12, 31)
          )
          dayOfToday <- Gen.choose(1, 30)
        } yield (
            afterFirstQuarterDate,
            afterFirstQuarterDate.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (afterFirstQuarterDate, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateAfterFirstQuarter(afterFirstQuarterDate) mustEqual true
      }
    }

    "must be false if start date is before 1 October" in {
      val dates: Gen[(LocalDate, LocalDate)] =
        for {
          dateBefore1stOct <- datesBetween(
            LocalDate.of(2021, 9, 1),
            LocalDate.of(2021, 9, 30)
          )
          dayOfToday <- Gen.choose(1, 30)
        } yield (
            dateBefore1stOct,
            dateBefore1stOct.plusMonths(1).withDayOfMonth(dayOfToday)
        )

      forAll(dates) {
        case (dateBefore1stOct, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isStartDateAfterFirstQuarter(dateBefore1stOct) mustEqual false
      }
    }
  }

  ".startDateBasedOnFirstSale" - {

    "must be the date of the first sale" - {

      "when today is in the same month as the date of the first sale" in {

        val dates: Gen[(LocalDate, LocalDate)] = for {
          dayOfMonthOfFirstSale <- Gen.choose(1, 26)
          firstSale             <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
          dayOfToday            <- Gen.choose(dayOfMonthOfFirstSale, firstSale.lengthOfMonth())
        } yield (firstSale.withDayOfMonth(dayOfMonthOfFirstSale), firstSale.withDayOfMonth(dayOfToday))

        forAll(dates) {
          case (firstSale, today) =>
            val stubClock = getStubClock(today)
            val service = new DateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual firstSale
        }
      }

      "when today is the 1st to the 10th of the month after the date of first sale" in {

        val dates: Gen[(LocalDate, LocalDate)] = for {
          firstSale  <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
          dayOfToday <- Gen.choose(1, 10)
        } yield (firstSale, firstSale.plusMonths(1).withDayOfMonth(dayOfToday))

        forAll(dates) {
          case (firstSale, today) =>
            val stubClock = getStubClock(today)
            val service = new DateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual firstSale
        }
      }
    }

    "must be the start of the next quarter" - {

      "when today is the 11th onwards of the month after the first sale" in {

        val dates: Gen[(LocalDate, LocalDate)] = for {
          firstSale  <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
          dayOfToday <- Gen.choose(11, firstSale.plusMonths(1).lengthOfMonth())
        } yield (firstSale, firstSale.plusMonths(1).withDayOfMonth(dayOfToday))

        forAll(dates) {
          case (firstSale, today) =>
            val stubClock = getStubClock(today)
            val service = new DateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextQuarter()
        }
      }

      "when today is two or more months after the first sale" in {

        val dates: Gen[(LocalDate, LocalDate)] = for {
          firstSale  <- datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2022, 12, 31))
          extraMonths <- Gen.choose(2, 12)
        } yield (firstSale, firstSale.plusMonths(extraMonths))

        forAll(dates) {
          case (firstSale, today) =>
            val stubClock = getStubClock(today)
            val service = new DateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextQuarter()
        }
      }
    }
  }

  ".earliestSaleAllowed" - {

    "between 1st July 2021 and 30th September 2021" - {

      "must be 1st July 2021" in {

        forAll(datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2021, 9, 30))) {
          date =>
            val stubClock = getStubClock(date)
            val service   = new DateService(stubClock)

            service.earliestSaleAllowed mustEqual LocalDate.of(2021, 7, 1)
        }
      }
    }

    "after 30th September 2021" - {

      "in the 1st to the 10th day of any quarter" - {

        "must be the 1st of the previous month" in {

          val dates = for {
            day   <- Gen.choose(1, 10)
            month <- Gen.oneOf(JANUARY, APRIL, JULY, OCTOBER).map(_.getValue)
            year  <- Gen.choose(2022, 2030)
          } yield LocalDate.of(year, month, day)

          forAll(dates) {
            date: LocalDate =>
              val stubClock = getStubClock(date)
              val service   = new DateService(stubClock)

              service.earliestSaleAllowed mustEqual date.minusMonths(1).withDayOfMonth(1)
          }
        }
      }

      "in the 11th day onwards of the first month of any quarter" - {

        "must be the first day of the quarter" in {

          val datesIn31DayMonths = for {
            day   <- Gen.choose(11, 31)
            month <- Gen.oneOf(JANUARY, JULY, OCTOBER).map(_.getValue)
            year  <- Gen.choose(2022, 2030)
          } yield LocalDate.of(year, month, day)

          val datesInApril = for {
            day  <- Gen.choose(11, 30)
            year <- Gen.choose(2022, 2030)
          } yield LocalDate.of(year, APRIL.getValue, day)

          val dates = Gen.oneOf(datesInApril, datesIn31DayMonths)

          forAll(dates) {
            date =>
              val stubClock = getStubClock(date)
              val service   = new DateService(stubClock)

              service.earliestSaleAllowed mustEqual date.withDayOfMonth(1)
          }
        }
      }

      "on any day after the first month of any quarter" - {

        "must be the first day of the quarter" in {

          val dates = for {
            year  <- Gen.choose(2022, 2030)
            month <- Gen.oneOf(FEBRUARY, MARCH, MAY, JUNE, AUGUST, SEPTEMBER, NOVEMBER, DECEMBER)
            day   <- Gen.choose(1, month.length(Year.of(year).isLeap))
            quarterStart = month match {
              case FEBRUARY | MARCH     => JANUARY
              case MAY      | JUNE      => APRIL
              case AUGUST   | SEPTEMBER => JULY
              case NOVEMBER | DECEMBER  => OCTOBER
            }
          } yield (LocalDate.of(year, month, day), LocalDate.of(year, quarterStart, 1))

          forAll(dates) {
            case (today, startOfQuarter) =>
              val stubClock = getStubClock(today)
              val service   = new DateService(stubClock)

              service.earliestSaleAllowed mustEqual startOfQuarter
          }
        }
      }
    }
  }
}
