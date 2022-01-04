/*
 * Copyright 2022 HM Revenue & Customs
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
import forms.SalesChannelsFormProvider
import models.SalesChannels
import pages.SalesChannelsPage
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.UnauthenticatedSessionRepository
import views.html.SalesChannelsView
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind

import scala.concurrent.Future

class SalesChannelsControllerSpec extends SpecBase with MockitoSugar {

  private lazy val salesChannelsRoute = routes.SalesChannelsController.onPageLoad().url

  private val formProvider = new SalesChannelsFormProvider()
  private val form = formProvider()

  "SalesChannels Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, salesChannelsRoute)

        val view = application.injector.instanceOf[SalesChannelsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form)(request, messages(application)).toString
      }
    }

    "must populate the view and return OK and the correct view for a GET when the question has already been answered" in {

      val answers = emptyUserAnswers.set(SalesChannelsPage, SalesChannels.Mixed).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, salesChannelsRoute)

        val view = application.injector.instanceOf[SalesChannelsView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(SalesChannels.Mixed))(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val sessionRepository = mock[UnauthenticatedSessionRepository]

      when(sessionRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[UnauthenticatedSessionRepository].toInstance(sessionRepository))
        .build()

      running(application) {
        val request =
          FakeRequest(POST, salesChannelsRoute)
            .withFormUrlEncodedBody(("value", SalesChannels.values.head.toString))

        val result = route(application, request).value
        val expectedAnswers = emptyUserAnswers.set(SalesChannelsPage, SalesChannels.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SalesChannelsPage.navigate(SalesChannels.values.head).url
        verify(sessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, salesChannelsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[SalesChannelsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm)(request, messages(application)).toString
      }
    }

    "must redirect to Registered for OSS in EU for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, salesChannelsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RegisteredForOssInEuController.onPageLoad().url
      }
    }

    "must redirect to Registered for OSS in EU for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, salesChannelsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.RegisteredForOssInEuController.onPageLoad().url
      }
    }
  }
}
