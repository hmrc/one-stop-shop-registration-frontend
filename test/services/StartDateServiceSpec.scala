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

import java.time.{Clock, LocalDate, ZoneId}

class StartDateServiceSpec extends SpecBase with ScalaCheckPropertyChecks with Generators {

  private def getStubClock(date: LocalDate): Clock =
    Clock.fixed(date.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)

  ".startOfNextPeriod" - {

    "must be 1st January of the next year for any date in October, November or December" in {

      forAll(datesBetween(LocalDate.of(2021, 10, 1), LocalDate.of(2021, 12, 31))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.startOfNextPeriod mustEqual LocalDate.of(2022, 1, 1)
      }
    }

    "must be 1st April for any date in January, February or March" in {

      forAll(datesBetween(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 3, 31))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.startOfNextPeriod mustEqual LocalDate.of(2022, 4, 1)
      }
    }

    "must be 1st July for any date in April, May or June" in {

      forAll(datesBetween(LocalDate.of(2022, 4, 1), LocalDate.of(2022, 6, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.startOfNextPeriod mustEqual LocalDate.of(2022, 7, 1)
      }
    }

    "must be 1st October for any date in July, August or September" in {

      forAll(datesBetween(LocalDate.of(2022, 7, 1), LocalDate.of(2022, 9, 30))) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.startOfNextPeriod mustEqual LocalDate.of(2022, 10, 1)
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
            val service = new StartDateService(stubClock)

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
            val service = new StartDateService(stubClock)

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
            val service = new StartDateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextPeriod
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
            val service = new StartDateService(stubClock)

            service.startDateBasedOnFirstSale(firstSale) mustEqual service.startOfNextPeriod
        }
      }
    }
  }
}
