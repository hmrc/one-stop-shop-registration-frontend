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

  ".canRegisterLastMonth" - {

    "must be true for any date before the 11th of the month" in {

      val dates: Gen[LocalDate] = for {
        dayOfMonth <- Gen.choose(1, 10)
        date       <- datesBetween(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
      } yield date.withDayOfMonth(dayOfMonth)

      forAll(dates) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.canRegisterLastMonth mustEqual true
      }
    }

    "must be false for any date after the 10th of the month" in {

      val dates: Gen[LocalDate] = for {
        date       <- datesBetween(LocalDate.of(2022, 1, 1), LocalDate.of(2022, 12, 31))
        dayOfMonth <- Gen.choose(11, date.lengthOfMonth)
      } yield date.withDayOfMonth(dayOfMonth)

      forAll(dates) {
        date =>
          val stubClock = getStubClock(date)
          val service   = new StartDateService(stubClock)

          service.canRegisterLastMonth mustEqual false
      }
    }
  }
}
