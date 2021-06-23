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
import config.Constants
import formats.Format.dateFormatter
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.DateOfFirstSalePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateService
import views.html.CommencementDateView

import java.time.{Clock, LocalDate, ZoneId}

class CommencementDateControllerSpec extends SpecBase with MockitoSugar {

  "CommencementDate Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET" in {

        val answers = emptyUserAnswers.set(DateOfFirstSalePage, arbitraryDate).success.value
        val dateService = mock[DateService]

        when(dateService.startDateBasedOnFirstSale(any())) thenReturn arbitraryDate

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, arbitraryDate.format(dateFormatter))(request, messages(application)).toString
        }
      }
    }

    "when the scheme has not started" - {

      "must return OK and the correct view for a GET" in {

        val instantBeforeSchemeStarts = LocalDate.of(2021, 6, 30).atStartOfDay(ZoneId.systemDefault).toInstant
        val clock = Clock.fixed(instantBeforeSchemeStarts, ZoneId.systemDefault)

        val application = applicationBuilder(Some(emptyUserAnswers), Some(clock)).build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, Constants.schemeStartDate.format(dateFormatter))(request, messages(application)).toString
        }
      }
    }
  }
}