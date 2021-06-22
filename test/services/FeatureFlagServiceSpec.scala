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

import generators.Generators
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.test.Helpers.running

import java.time.{Clock, LocalDate, ZoneId}

class FeatureFlagServiceSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators {

  ".schemeHasStarted" - {

    "must be false before 1st July 2021" in {

      forAll(datesBetween(LocalDate.of(2021, 4, 1), LocalDate.of(2021, 6, 30))) {
        date =>
          val stubClock = Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

          val application =
            new GuiceApplicationBuilder()
              .overrides(bind[Clock].toInstance(stubClock))
              .build()

          running(application) {

            val service = application.injector.instanceOf[FeatureFlagService]
            service.schemeHasStarted mustEqual false
          }
      }
    }

    "must be true on or after 1st July 2021" in {

      forAll(datesBetween(LocalDate.of(2021, 7, 1), LocalDate.of(2030, 1, 1))) {
        date =>
          val stubClock = Clock.fixed(date.atStartOfDay(ZoneId.systemDefault()).toInstant, ZoneId.systemDefault())

          val application =
            new GuiceApplicationBuilder()
              .overrides(bind[Clock].toInstance(stubClock))
              .build()

          running(application) {

            val service = application.injector.instanceOf[FeatureFlagService]
            service.schemeHasStarted mustEqual true
          }
      }
    }
  }
}
