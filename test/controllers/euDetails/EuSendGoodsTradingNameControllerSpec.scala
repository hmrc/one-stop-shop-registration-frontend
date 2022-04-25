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
import forms.euDetails.EuSendGoodsTradingNameFormProvider
import models.{Country, Index, NormalMode, UserAnswers}
import org.scalacheck.Arbitrary.arbitrary
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.{EuCountryPage, EuSendGoodsTradingNamePage}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.euDetails.EuSendGoodsTradingNameView

class EuSendGoodsTradingNameControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)
  private val country: Country = arbitrary[Country].sample.value
  private val formProvider = new EuSendGoodsTradingNameFormProvider()
  private val form = formProvider(country)

  private lazy val euSendGoodsTradingNameRoute = routes.EuSendGoodsTradingNameController.onPageLoad(NormalMode, index).url

  private val baseUserAnswers = basicUserAnswers.set(EuCountryPage(index), country).success.value

  "EuSendGoodsTradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EuSendGoodsTradingNameView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(EuSendGoodsTradingNamePage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsTradingNameRoute)

        val view = application.injector.instanceOf[EuSendGoodsTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill("answer"), NormalMode, index, country)(request, messages(application)).toString
      }
    }

//    "must save the answer and redirect to the next page when valid data is submitted" in {
//
//      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]
//
//      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)
//
//      val application =
//        applicationBuilder(userAnswers = Some(emptyUserAnswers))
//          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
//          .build()
//
//      running(application) {
//        val request =
//          FakeRequest(POST, euSendGoodsTradingNameRoute)
//            .withFormUrlEncodedBody(("value", "answer"))
//
//        val result = route(application, request).value
//        val expectedAnswers = emptyUserAnswers.set(EuSendGoodsTradingNamePage, "answer").success.value
//
//        status(result) mustEqual SEE_OTHER
//        redirectLocation(result).value mustEqual EuSendGoodsTradingNamePage.navigate(NormalMode, expectedAnswers).url
//        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
//      }
//    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsTradingNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuSendGoodsTradingNameView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsTradingNameRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsTradingNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
