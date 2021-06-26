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
import forms.RegisteredForOssInEuFormProvider
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.RegisteredForOssInEuPage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UnauthenticatedSessionRepository
import views.html.RegisteredForOssInEuView

import scala.concurrent.Future

class RegisteredForOssInEuControllerSpec extends SpecBase with MockitoSugar {

  private lazy val formProvider = new RegisteredForOssInEuFormProvider()
  private lazy val form = formProvider()

  private lazy val controllerRoute = routes.RegisteredForOssInEuController.onPageLoad().url

  "RegisteredForOssInEu Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, controllerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegisteredForOssInEuView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view and return OK and the correct view for a GET when the question has been answered" in {

      val answers = emptyUserAnswers.set(RegisteredForOssInEuPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, controllerRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegisteredForOssInEuView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true))(request, messages(application)).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val sessionRepository = mock[UnauthenticatedSessionRepository]
      when(sessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(emptyUserAnswers))
          .overrides(bind[UnauthenticatedSessionRepository].toInstance(sessionRepository))
          .build()

      running(application) {
        val request = FakeRequest(POST, controllerRoute).withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RegisteredForOssInEuPage.navigate(true).url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, controllerRoute).withFormUrlEncodedBody(("value", "invalid"))

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegisteredForOssInEuView]
        val boundForm = form.bind(Map("value" -> "invalid"))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString      }
    }
  }
}
