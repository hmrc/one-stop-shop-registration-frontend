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
import forms.HasMadeSalesFormProvider
import models.NormalMode
import models.SalesChannels.Mixed
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{BusinessBasedInNiPage, HasFixedEstablishmentInNiPage, HasMadeSalesPage, SalesChannelsPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedSessionRepository
import views.html.HasMadeSalesView

import scala.concurrent.Future
import org.scalatest.PrivateMethodTester

class HasMadeSalesControllerSpec extends SpecBase with MockitoSugar with PrivateMethodTester {

  private val formProvider = new HasMadeSalesFormProvider()
  private val form = formProvider()

  private lazy val hasMadeSalesRoute = routes.HasMadeSalesController.onPageLoad(NormalMode).url

  "HasMadeSales Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasMadeSalesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasMadeSalesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must populate the view and return OK and the correct view for a GET when the question has already been answered" in {

      val answers = basicUserAnswers.set(HasMadeSalesPage, true).success.value
      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, hasMadeSalesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasMadeSalesView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(true), NormalMode, false)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val sessionRepository = mock[AuthenticatedSessionRepository]
      when(sessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswers))
          .overrides(bind[AuthenticatedSessionRepository].toInstance(sessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, hasMadeSalesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        val expectedAnswers = basicUserAnswers.set(HasMadeSalesPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual HasMadeSalesPage.navigate(NormalMode, expectedAnswers).url
        verify(sessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, hasMadeSalesRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[HasMadeSalesView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, false)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, hasMadeSalesRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, hasMadeSalesRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "showHintText method must return true when sales are not included from online marketplaces" in {

      val answerBusinessBasedInNiPage = basicUserAnswers.set(BusinessBasedInNiPage, false).success.value
      val answerHasFixedEstablishmentInNiPage = answerBusinessBasedInNiPage.set(HasFixedEstablishmentInNiPage, false).success.value
      val userAnswers = answerHasFixedEstablishmentInNiPage.set(SalesChannelsPage, Mixed).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasMadeSalesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasMadeSalesView]

        val controller = application.injector.instanceOf[HasMadeSalesController]

        val showHintTextMethod = PrivateMethod[Boolean]('showHintText)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controller invokePrivate showHintTextMethod(userAnswers)
        )(request, messages(application)).toString
      }
    }

    "showHintText method must return false when sales are included from online marketplaces" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, hasMadeSalesRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[HasMadeSalesView]

        val controller = application.injector.instanceOf[HasMadeSalesController]

        val showHintTextMethod = PrivateMethod[Boolean]('showHintText)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          form,
          NormalMode,
          controller invokePrivate showHintTextMethod(basicUserAnswers)
        )(request, messages(application)).toString
      }
    }
  }

}
