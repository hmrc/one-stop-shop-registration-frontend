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

package controllers.rejoin

import base.SpecBase
import config.FrontendAppConfig
import formats.Format.dateFormatter
import models.Country.getCountryName
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.rejoin.CannotRejoinQuarantinedCountryView

import java.time.LocalDate

class CannotRejoinQuarantinedCountryControllerSpec extends SpecBase {

  private val countryCode: String = arbitraryCountry.arbitrary.sample.value.code
  private val countryName: String = getCountryName(countryCode)
  private val earliestRejoinDate: String = LocalDate.now().format(dateFormatter)

  "CannotRejoinQuarantinedCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val config = application.injector.instanceOf[FrontendAppConfig]

        val request = FakeRequest(GET, routes.CannotRejoinQuarantinedCountryController.onPageLoad(countryCode, earliestRejoinDate).url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotRejoinQuarantinedCountryView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(config.ossYourAccountUrl, countryName, earliestRejoinDate)(request, messages(application)).toString
      }
    }
  }
}
