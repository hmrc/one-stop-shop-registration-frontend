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

package controllers.euDetails

import base.SpecBase
import forms.euDetails.AddEuDetailsFormProvider
import models.euDetails.EuOptionalDetails
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{AddEuDetailsPage, EuCountryPage, HasFixedEstablishmentPage, VatRegisteredPage}
import play.api.i18n.Messages
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import viewmodels.checkAnswers.euDetails.EuDetailsSummary
import views.html.euDetails.AddEuDetailsView

import scala.concurrent.Future

class AddEuDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new AddEuDetailsFormProvider()
  private val form = formProvider()
  private val index = Index(0)
  private val country               = Country.euCountries.head

  private lazy val addEuVatDetailsRoute = routes.AddEuDetailsController.onPageLoad(NormalMode).url
  private def addEuVatDetailsPostRoute(prompt: Boolean = false) = routes.AddEuDetailsController.onSubmit(NormalMode, prompt).url
  private val baseAnswers =
    basicUserAnswers
      .set(EuCountryPage(index), country).success.value
      .set(VatRegisteredPage(index), false).success.value
      .set(HasFixedEstablishmentPage(index), false).success.value

  private val incompleteAnswers =
    basicUserAnswers
      .set(EuCountryPage(index), country).success.value
      .set(VatRegisteredPage(index), true).success.value

  "AddEuDetails Controller" - {

    "must return OK and the correct view for a GET when answers are complete" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.addToListRows(baseAnswers, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must return OK and the correct view for a GET when answers are incomplete" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        val view                    = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.addToListRows(incompleteAnswers, NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, list, canAddCountries = true,
          Seq(EuOptionalDetails(country, Some(true), None, None, None, None, None))
        )(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseAnswers.set(AddEuDetailsPage, true).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val view                    = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) must not be view(form.fill(true), NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseAnswers.set(AddEuDetailsPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual AddEuDetailsPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view                    = application.injector.instanceOf[AddEuDetailsView]
        implicit val msgs: Messages = messages(application)
        val list                    = EuDetailsSummary.addToListRows(baseAnswers, NormalMode)

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, list, canAddCountries = true)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if user answers are empty" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, addEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must refresh the page for a POST if answers are incomplete and the prompt has not been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute())
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.AddEuDetailsController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to CheckEuDetailsAnswers page for a POST if answers are incomplete and the prompt has been shown" in {

      val application = applicationBuilder(userAnswers = Some(incompleteAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, addEuVatDetailsPostRoute(true))
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckEuDetailsAnswersController.onPageLoad(NormalMode, index).url
      }
    }
  }
}
