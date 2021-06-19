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

import forms.behaviours.BooleanFieldBehaviours
import generators.Generators
import models.AlreadyMadeSales
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.FormError

import java.time.{Clock, LocalDate, ZoneId}

class AlreadyMadeSalesFormProviderSpec extends BooleanFieldBehaviours with ScalaCheckPropertyChecks with Generators {

  private val stubClock = Clock.fixed(LocalDate.now.atStartOfDay(ZoneId.systemDefault).toInstant, ZoneId.systemDefault)

  private val form = new AlreadyMadeSalesFormProvider(stubClock)()

  "form" - {

    "when the answer is false" - {

      "must bind" in {

        val result = form.bind(Map("answer" -> "false"))
        result.value.value mustEqual AlreadyMadeSales(answer = false, None)
      }
    }

    "when the answer is true" - {

      "must bind if firstSale is in the past or today" in {

        forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
          date =>
            val data = Map(
              "answer"          -> "true",
              "firstSale.day"   -> date.getDayOfMonth.toString,
              "firstSale.month" -> date.getMonthValue.toString,
              "firstSale.year"  -> date.getYear.toString
            )

            val result = form.bind(data)

            result.value.value mustEqual AlreadyMadeSales(answer = true, Some(date))
            result.errors mustBe empty
        }
      }

      "must fail to bind" - {

        "when firstSale is not present" in {

          val result = form.bind(Map("answer" -> "true"))
          result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.allRequired")
        }

        "when firstSale is in the future" in {

          forAll(datesBetween(LocalDate.now, LocalDate.now.plusYears(1))) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.day"   -> date.getDayOfMonth.toString,
                "firstSale.month" -> date.getMonthValue.toString,
                "firstSale.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.maxDate")
          }
        }

        "when firstSale.day is missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.month" -> date.getMonthValue.toString,
                "firstSale.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.required", Seq("day"))
          }
        }

        "when firstSale.month is missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.day"   -> date.getDayOfMonth.toString,
                "firstSale.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.required", Seq("month"))
          }
        }

        "when firstSale.year is missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.day"   -> date.getDayOfMonth.toString,
                "firstSale.month" -> date.getMonthValue.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.required", Seq("year"))
          }
        }

        "when firstSale.day and firstSale.month are missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.year"  -> date.getYear.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.twoRequired", Seq("day", "month"))
          }
        }

        "when firstSale.day and firstSale.year are missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"          -> "true",
                "firstSale.month" -> date.getMonthValue.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.twoRequired", Seq("day", "year"))
          }
        }

        "when firstSale.month and firstSale.year are missing" in {

          forAll(datesBetween(LocalDate.now.minusYears(1), LocalDate.now)) {
            date =>
              val data = Map(
                "answer"        -> "true",
                "firstSale.day" -> date.getDayOfMonth.toString
              )

              val result = form.bind(data)

              result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.twoRequired", Seq("month", "year"))
          }
        }

        "when firstSale is not a valid date" in {

          val data = Map(
            "answer"          -> "true",
            "firstSale.day"   -> "32",
            "firstSale.month" -> LocalDate.now.getMonthValue.toString,
            "firstSale.year"  -> LocalDate.now.getYear.toString
          )

          val result = form.bind(data)

          result.errors must contain only FormError("firstSale", "alreadyMadeSales.firstSale.error.invalid")
        }
      }
    }

    "must fail to bind when answer is not present" in {

      forAll(datesBetween(LocalDate.now.minusMonths(1), LocalDate.now)) {
        date =>
          val data = Map(
            "firstSale.day"   -> date.getDayOfMonth.toString,
            "firstSale.month" -> date.getMonthValue.toString,
            "firstSale.year"  -> date.getYear.toString
          )

          val result = form.bind(data)

          result.errors must contain only FormError("answer", "alreadyMadeSales.answer.error.required")
      }
    }
  }
}
