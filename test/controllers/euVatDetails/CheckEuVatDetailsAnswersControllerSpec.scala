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

package controllers.euVatDetails

import base.SpecBase
import models.{Country, Index, NormalMode}
import pages.euVatDetails
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.checkAnswers.euVatDetails.EuCountrySummary
import viewmodels.govuk.SummaryListFluency
import views.html.euVatDetails.CheckEuVatDetailsAnswersView

class CheckEuVatDetailsAnswersControllerSpec extends SpecBase with SummaryListFluency {

  private val index           = Index(0)
  private val country         = Country.euCountries.head
  private val baseUserAnswers = emptyUserAnswers.set(euVatDetails.EuCountryPage(index), country).success.value

  "CheckEuVatDetailsAnswersController" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        implicit val msgs: Messages = messages(application)
        val request = FakeRequest(GET, routes.CheckEuVatDetailsAnswersController.onPageLoad(index).url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[CheckEuVatDetailsAnswersView]
        val list = SummaryListViewModel(
          Seq(EuCountrySummary.row(baseUserAnswers, index)).flatten
        )

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(list, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to Add Eu VAT Details on a POST" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, routes.CheckEuVatDetailsAnswersController.onSubmit(index).url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AddEuVatDetailsController.onPageLoad(NormalMode).url
      }
    }
  }
}
