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
import forms.euDetails.SellsGoodsToEUConsumerMethodFormProvider
import models.euDetails.EuConsumerSalesMethod
import models.{Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, SellsGoodsToEUConsumerMethodPage, SellsGoodsToEUConsumersPage, TaxRegisteredInEuPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.euDetails.SellsGoodsToEUConsumerMethodView

import scala.concurrent.Future

class SellsGoodsToEUConsumerMethodControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new SellsGoodsToEUConsumerMethodFormProvider()
  private val country: Country = Country.euCountries.head
  private val form = formProvider(country)

  private val countryIndex: Index = Index(0)

  private lazy val sellsGoodsToEUConsumerMethodRoute = routes.SellsGoodsToEUConsumerMethodController.onPageLoad(NormalMode, countryIndex).url

  private val answers = emptyUserAnswers.set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(countryIndex), country).success.value
    .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value

  "SellsGoodsToEUConsumerMethod Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request = FakeRequest(GET, sellsGoodsToEUConsumerMethodRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[SellsGoodsToEUConsumerMethodView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = answers.set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.FixedEstablishment).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, sellsGoodsToEUConsumerMethodRoute)

        val view = application.injector.instanceOf[SellsGoodsToEUConsumerMethodView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(EuConsumerSalesMethod.FixedEstablishment), NormalMode, countryIndex, country)(request, messages(application)).toString
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
          FakeRequest(POST, sellsGoodsToEUConsumerMethodRoute)
            .withFormUrlEncodedBody(("value", EuConsumerSalesMethod.values.head.toString))

        val result = route(application, request).value
        val expectedAnswers = answers.set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.values.head).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual SellsGoodsToEUConsumerMethodPage(countryIndex).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(answers)).build()

      running(application) {
        val request =
          FakeRequest(POST, sellsGoodsToEUConsumerMethodRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[SellsGoodsToEUConsumerMethodView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, countryIndex, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, sellsGoodsToEUConsumerMethodRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, sellsGoodsToEUConsumerMethodRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
