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
import forms.AddAdditionalEuVatDetailsFormProvider
import models.{Country, Index, NormalMode}
import navigation.{FakeNavigator, Navigator}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar
import pages.{AddAdditionalEuVatDetailsPage, EuVatNumberPage, VatRegisteredEuMemberStatePage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.mvc.Call
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import viewmodels.checkAnswers.EuVatDetailsSummary
import views.html.AddAdditionalEuVatDetailsView

import scala.concurrent.Future

class AddAdditionalEuVatDetailsControllerSpec extends SpecBase with MockitoSugar {

  private def onwardRoute = Call("GET", "/foo")

  private val formProvider = new AddAdditionalEuVatDetailsFormProvider()
  private val form = formProvider()

  private lazy val addAdditionalEuVatDetailsRoute = routes.AddAdditionalEuVatDetailsController.onPageLoad(NormalMode).url

  private val baseAnswers =
    emptyUserAnswers
      .set(VatRegisteredEuMemberStatePage(Index(0)), Country.euCountries.head).success.value
      .set(EuVatNumberPage(Index(0)), "foo").success.value

  "AddAdditionalEuVatDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addAdditionalEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[AddAdditionalEuVatDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuVatDetailsSummary.addToListRows(baseAnswers)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddAdditionalEuVatDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addAdditionalEuVatDetailsRoute)

        val view                    = application.injector.instanceOf[AddAdditionalEuVatDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuVatDetailsSummary.addToListRows(baseAnswers)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[SessionRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(
            bind[Navigator].toInstance(new FakeNavigator(onwardRoute)),
            bind[SessionRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addAdditionalEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual onwardRoute.url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addAdditionalEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view                    = application.injector.instanceOf[AddAdditionalEuVatDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuVatDetailsSummary.addToListRows(baseAnswers)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addAdditionalEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addAdditionalEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
