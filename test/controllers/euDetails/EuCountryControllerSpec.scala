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

package controllers.euDetails

import base.SpecBase
import forms.euDetails.EuCountryFormProvider
import models.{Country, Index, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails
import pages.euDetails.{EuCountryPage, SellsGoodsToEUConsumersPage, VatRegisteredPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import views.html.euDetails.EuCountryView

class EuCountryControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)

  private val formProvider = new EuCountryFormProvider()
  private val form = formProvider(index, Seq.empty)

  private lazy val euCountryRoute = routes.EuCountryController.onPageLoad(NormalMode, index).url

  private val country = Country.euCountries.head

  "EUCountry Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, euCountryRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EuCountryView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(euDetails.EuCountryPage(index), country).success.value
        .set(SellsGoodsToEUConsumersPage(index), false).success.value
        .set(VatRegisteredPage(index), false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euCountryRoute)

        val view = application.injector.instanceOf[EuCountryView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill(country), NormalMode, index)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, euCountryRoute)
            .withFormUrlEncodedBody(("value", country.code))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswersWithVatInfo.set(EuCountryPage(index), country).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` EuCountryPage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request =
          FakeRequest(POST, euCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuCountryView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must return a Bad Request and errors when invalid data is submitted and question has been answered" in {

      val userAnswers = UserAnswers(userAnswersId)
        .set(euDetails.EuCountryPage(index), country).success.value
        .set(SellsGoodsToEUConsumersPage(index), false).success.value
        .set(VatRegisteredPage(index), false).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, euCountryRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuCountryView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index)(request, messages(application)).toString
      }
    }
  }
}
