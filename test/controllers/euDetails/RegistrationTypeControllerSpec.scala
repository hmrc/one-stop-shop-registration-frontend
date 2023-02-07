/*
 * Copyright 2023 HM Revenue & Customs
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
import forms.euDetails.RegistrationTypeFormProvider
import models.euDetails.{EUConsumerSalesMethod, RegistrationType}
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, RegistrationTypePage, SellsGoodsToEUConsumerMethodPage, SellsGoodsToEUConsumersPage, TaxRegisteredInEuPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.euDetails.RegistrationTypeView

import scala.concurrent.Future

class RegistrationTypeControllerSpec extends SpecBase with MockitoSugar {

  private val countryIndex: Index = Index(0)

  private lazy val registrationTypeRoute = routes.RegistrationTypeController.onPageLoad(NormalMode, countryIndex).url

  private val country = Country.euCountries.head
  private val formProvider = new RegistrationTypeFormProvider()
  private val form = formProvider(country)

  private val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(countryIndex), country).success.value
    .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
    .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EUConsumerSalesMethod.FixedEstablishment).success.value

  "RegistrationType Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, registrationTypeRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[RegistrationTypeView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = answers.set(RegistrationTypePage(countryIndex), RegistrationType.values.head).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, registrationTypeRoute)

        val view = application.injector.instanceOf[RegistrationTypeView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(RegistrationType.values.head), NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", RegistrationType.values.head.toString))

        val result = route(application, request).value
        val expectedAnswers = answers.set(RegistrationTypePage(countryIndex), RegistrationType.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual RegistrationTypePage(countryIndex).navigate(RegistrationType.values.head).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[RegistrationTypeView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, registrationTypeRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, registrationTypeRoute)
            .withFormUrlEncodedBody(("value", RegistrationType.values.head.toString))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER

        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
