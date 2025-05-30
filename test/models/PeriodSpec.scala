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

package models

import generators.Generators
import models.Quarter.*
import org.mockito.Mockito.when
import org.scalacheck.Arbitrary.arbitrary
import org.scalacheck.Gen
import org.scalatest.EitherValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.i18n.Messages
import play.api.mvc.{PathBindable, QueryStringBindable}

import java.time.LocalDate
import java.time.Month.*
import java.time.format.DateTimeFormatter

class PeriodSpec
  extends AnyFreeSpec
    with Matchers
    with ScalaCheckPropertyChecks
    with Generators
    with EitherValues {

  private val pathBindable = implicitly[PathBindable[Period]]
  private val queryBindable = implicitly[QueryStringBindable[Period]]
  private val year = 2024

  "Period" - {
    "pathBindable" - {
      "must bind from a URL" in {

        forAll(arbitrary[Period]) {
          period =>

            pathBindable.bind("key", period.toString).value mustEqual period
        }
      }

      "must not bind from an invalid value" in {

        pathBindable.bind("key", "invalid").left.value mustEqual "Invalid period"
      }
    }

    "queryBindable" - {
      "must bind from a query parameter when valid period present" in {

        forAll(arbitrary[Period]) {
          period =>

            queryBindable.bind("key", Map("key" -> Seq(period.toString))) mustBe Some(Right(period))
        }
      }

      "must not bind from an invalid value" in {

        queryBindable.bind("key", Map("key" -> Seq("invalid"))) mustBe Some(Left("Invalid period"))
      }

      "must return none if no query parameter present" in {
        queryBindable.bind("key", Map("key" -> Seq.empty)) mustBe None
      }
    }


  }

  ".firstDay" - {

    "must be the first of January when the quarter is Q1" in {

      forAll(Gen.choose(2022, 2100)) {
        year =>
          val period = Period(year, Q1)
          period.firstDay mustEqual LocalDate.of(year, JANUARY, 1)
      }
    }

    "must be the first of April when the quarter is Q2" in {

      forAll(Gen.choose(2022, 2100)) {
        year =>
          val period = Period(year, Q2)
          period.firstDay mustEqual LocalDate.of(year, APRIL, 1)
      }
    }

    "must be the first of July when the quarter is Q3" in {

      forAll(Gen.choose(2021, 2100)) {
        year =>
          val period = Period(year, Q3)
          period.firstDay mustEqual LocalDate.of(year, JULY, 1)
      }
    }

    "must be the first of October when the quarter is Q4" in {

      forAll(Gen.choose(2021, 2100)) {
        year =>
          val period = Period(year, Q4)
          period.firstDay mustEqual LocalDate.of(year, OCTOBER, 1)
      }
    }
  }

  ".lastDay" - {

    "must be the 31st of March when the quarter is Q1" in {

      forAll(Gen.choose(2022, 2100)) {
        year =>
          val period = Period(year, Q1)
          period.lastDay mustEqual LocalDate.of(year, MARCH, 31)
      }
    }

    "must be the 30th of June when the quarter is Q2" in {

      forAll(Gen.choose(2022, 2100)) {
        year =>
          val period = Period(year, Q2)
          period.lastDay mustEqual LocalDate.of(year, JUNE, 30)
      }
    }

    "must be the 30th of September when the quarter is Q3" in {

      forAll(Gen.choose(2021, 2100)) {
        year =>
          val period = Period(year, Q3)
          period.lastDay mustEqual LocalDate.of(year, SEPTEMBER, 30)
      }
    }

    "must be the 31st of December when the quarter is Q4" in {

      forAll(Gen.choose(2021, 2100)) {
        year =>
          val period = Period(year, Q4)
          period.lastDay mustEqual LocalDate.of(year, DECEMBER, 31)
      }
    }
  }


  "displayText" - {

    "return formatted date range correctly" in {

      implicit val messages: Messages = mock[Messages]
      when(messages("site.to")).thenReturn("to")


      val quarter = Q1
      val firstDay = LocalDate.of(year, quarter.startMonth, 1)
      val lastDay = firstDay.plusMonths(3).minusDays(1)

      val firstDayFormatter = DateTimeFormatter.ofPattern("d MMMM")
      val lastDayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")

      val expectedText = s"${firstDay.format(firstDayFormatter)} to ${lastDay.format(lastDayFormatter)}"

      val period = Period(year, quarter)
      period.displayText mustBe expectedText
    }
  }

  "getPreviousPeriod" - {

    val previousYear = 2023

    "return the correct previous quarter" in {
      Period(year, Q4).getPreviousPeriod mustBe Period(year, Q3)
      Period(year, Q3).getPreviousPeriod mustBe Period(year, Q2)
      Period(year, Q2).getPreviousPeriod mustBe Period(year, Q1)
      Period(year, Q1).getPreviousPeriod mustBe Period(previousYear, Q4)
    }
  }

}
