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

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextQuarter
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

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextQuarter
        }
      }
    }
  }

  ".isDateOfFirstSaleDifferentToCommencementDate" - {

    "must return true if the Date Of First Sale is a different date to the Commencement Date" in {
      val dates: Gen[(LocalDate, LocalDate)] = for {
        dateOfFirstSale  <- datesBetween(
          LocalDate.of(2021, 7, 1),
          LocalDate.now().minusDays(1)
        )
        extraMonths <- Gen.choose(2, 12)
      } yield (dateOfFirstSale, dateOfFirstSale.plusMonths(extraMonths))

      val commencementDate = LocalDate.now()

      forAll(dates) {
        case (dateOfFirstSale, today) =>
          val stubClock = getStubClock(today)
          val service = new DateService(stubClock)

          service.isDOFSDifferentToCommencementDate(Some(dateOfFirstSale), commencementDate) mustEqual true
      }
    }

    "must return false if the Date Of First Sale is the same date as the Commencement Date" in {
      val service = new DateService(getStubClock(LocalDate.now()))
      service.isDOFSDifferentToCommencementDate(Some(LocalDate.now()), LocalDate.now()) mustEqual false
    }

    "must return false if the Date Of First Sale is empty" in {
      val service = new DateService(getStubClock(LocalDate.now()))
      service.isDOFSDifferentToCommencementDate(None, LocalDate.now()) mustEqual false
    }
  }

  "getVatReturnEndDate" - {

    "must return the end of the quarter based on commencement date" in {
      val commencementDate = LocalDate.of(2021, 8, 11)
      val expectedVatReturnEndDate = LocalDate.of(2021, 9, 30)
      val service = new DateService(getStubClock(LocalDate.now()))

      val vatReturnEndDate = service.getVatReturnEndDate(commencementDate)

      vatReturnEndDate mustEqual expectedVatReturnEndDate
    }

  }

  "getVatReturnDeadline" - {

    "must return the end of the month following the VAT Return end date" in {
      val vatReturnsEndDate = LocalDate.of(2021, 9, 30)
      val expectedVatReturnDeadline = LocalDate.of(2021, 10, 31)
      val service = new DateService(getStubClock(LocalDate.now()))

      val vatReturnDeadline = service.getVatReturnDeadline(vatReturnsEndDate)

      vatReturnDeadline mustEqual expectedVatReturnDeadline
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
