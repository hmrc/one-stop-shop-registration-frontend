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
import formats.Format.dateFormatter
import models.NormalMode
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{DateOfFirstSalePage, HasMadeSalesPage, IsPlanningFirstEligibleSalePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.DateService
import views.html.CommencementDateView

import java.time.LocalDate

class CommencementDateControllerSpec extends SpecBase with MockitoSugar {

  "CommencementDate Controller" - {

    "when the scheme has started" - {

      "must return OK and the correct view for a GET when user enters date" in {

        val answer1 = emptyUserAnswers.set(HasMadeSalesPage, true).success.value
        val answers = answer1.set(DateOfFirstSalePage, arbitraryStartDate).success.value

        val dateService = mock[DateService]

        when(dateService.startDateBasedOnFirstSale(any())) thenReturn arbitraryStartDate

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .overrides(bind[DateService].toInstance(dateService))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, arbitraryStartDate.format(dateFormatter))(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET when user answers yes to Is Planning First Eligible Sale and today's date is generated" in {

        val answer1 = emptyUserAnswers.set(HasMadeSalesPage, false).success.value
        val answers = answer1.set(IsPlanningFirstEligibleSalePage, true).success.value

        val application =
          applicationBuilder(userAnswers = Some(answers))
            .build()

        running(application) {
          val request = FakeRequest(GET, routes.CommencementDateController.onPageLoad(NormalMode).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CommencementDateView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, LocalDate.now().format(dateFormatter))(request, messages(application)).toString
        }
      }
    }
  }
}