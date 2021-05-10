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
import forms.IsBusinessBasedInNorthernIrelandFormProvider
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.IsBusinessBasedInNorthernIrelandView

class IsBusinessBasedInNorthernIrelandControllerSpec extends SpecBase {

  private val formProvider = new IsBusinessBasedInNorthernIrelandFormProvider()
  private val form = formProvider()

  private lazy val isBusinessBasedInNorthernIrelandRoute = routes.IsBusinessBasedInNorthernIrelandController.onPageLoad().url

  "IsBusinessBasedInNorthernIreland Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder().build()

      running(application) {
        val request = FakeRequest(GET, isBusinessBasedInNorthernIrelandRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[IsBusinessBasedInNorthernIrelandView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the cannot register for service page when false is selected" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isBusinessBasedInNorthernIrelandRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CannotRegisterForServiceController.onPageLoad().url
      }
    }

    "must redirect to the start page when true is selected" in {

      val application = applicationBuilder().build()

      running(application) {
        val request =
          FakeRequest(POST, isBusinessBasedInNorthernIrelandRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.IndexController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, isBusinessBasedInNorthernIrelandRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[IsBusinessBasedInNorthernIrelandView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
