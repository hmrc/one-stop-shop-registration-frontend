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

package controllers

import base.SpecBase
import forms.TradingNameFormProvider
import models.{Index, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalacheck.Gen
import org.scalatestplus.mockito.MockitoSugar
import pages.TradingNamePage
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.TradingNameView

import utils.FutureSyntax.FutureOps

class TradingNameControllerSpec extends SpecBase with MockitoSugar {

  private val index = Index(0)

  private val formProvider = new TradingNameFormProvider()
  private val form = formProvider(index, Seq.empty)

  private lazy val tradingNameRoute = routes.TradingNameController.onPageLoad(NormalMode, index).url

  "TradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, tradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[TradingNameView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = UserAnswers(userAnswersId).set(TradingNamePage(index), "answer").success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, tradingNameRoute)

        val view = application.injector.instanceOf[TradingNameView]

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form.fill("answer"), NormalMode, index)(request, messages(application)).toString
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
          FakeRequest(POST, tradingNameRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswersWithVatInfo.set(TradingNamePage(index), "answer").success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` TradingNamePage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request =
          FakeRequest(POST, tradingNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[TradingNameView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index)(request, messages(application)).toString
      }
    }

    "must return NOT_FOUND for a GET with an index of position 10 or greater" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()
      val highIndex = Gen.choose(10, Int.MaxValue).map(Index(_)).sample.value

      running(application) {

        val request = FakeRequest(GET, routes.TradingNameController.onPageLoad(NormalMode, highIndex).url)

        val result = route(application, request).value

        status(result) `mustBe` NOT_FOUND
      }
    }

    "must return NOT_FOUND for a POST with an index of position 10 or greater" in {

      val answers =
        basicUserAnswersWithVatInfo
          .set(TradingNamePage(Index(0)), "foo").success.value
          .set(TradingNamePage(Index(1)), "foo").success.value
          .set(TradingNamePage(Index(2)), "foo").success.value
          .set(TradingNamePage(Index(3)), "foo").success.value
          .set(TradingNamePage(Index(4)), "foo").success.value
          .set(TradingNamePage(Index(5)), "foo").success.value
          .set(TradingNamePage(Index(6)), "foo").success.value
          .set(TradingNamePage(Index(7)), "foo").success.value
          .set(TradingNamePage(Index(8)), "foo").success.value
          .set(TradingNamePage(Index(9)), "foo").success.value

      val application = applicationBuilder(userAnswers = Some(answers)).build()
      val highIndex = Gen.choose(10, Int.MaxValue).map(Index(_)).sample.value

      running(application) {

        val request =
          FakeRequest(POST, routes.TradingNameController.onPageLoad(NormalMode, highIndex).url)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) `mustBe` NOT_FOUND
      }
    }
  }
}
