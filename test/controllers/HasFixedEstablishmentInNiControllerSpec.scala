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
import forms.HasFixedEstablishmentInNiFormProvider
import org.scalatestplus.mockito.MockitoSugar
import pages.HasFixedEstablishmentInNiPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.HasFixedEstablishmentInNiView

class HasFixedEstablishmentInNiControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new HasFixedEstablishmentInNiFormProvider()
  private val form = formProvider()

  private lazy val hasFixedEstablishmentInNiRoute = routes.HasFixedEstablishmentInNiController.onPageLoad().url

  "HasFixedEstablishmentInNi Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasFixedEstablishmentInNiRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasFixedEstablishmentInNiView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasFixedEstablishmentInNiRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HasFixedEstablishmentInNiPage.navigate(true).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasFixedEstablishmentInNiRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HasFixedEstablishmentInNiView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }
  }
}
