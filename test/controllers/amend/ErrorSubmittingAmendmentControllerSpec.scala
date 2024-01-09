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

package controllers.amend

import base.SpecBase
import controllers.amend.{routes => amendRoutes}
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.amend.ErrorSubmittingAmendment

class ErrorSubmittingAmendmentControllerSpec extends SpecBase with MockitoSugar {

  "ErrorSubmittingAmendment Controller" - {

      "must return OK and the correct view" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, amendRoutes.ErrorSubmittingAmendmentController.onPageLoad().url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[ErrorSubmittingAmendment]

          status(result) mustEqual OK
          contentAsString(result) mustEqual
            view("http://localhost:10204/pay-vat-on-goods-sold-to-eu/northern-ireland-returns-payments/")(request, messages(application)).toString
        }
      }

  }

}
