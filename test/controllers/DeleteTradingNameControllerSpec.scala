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
import controllers.amend.routes as amendRoutes
import forms.DeleteTradingNameFormProvider
import models.{AmendMode, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{DeleteTradingNamePage, TradingNamePage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import views.html.DeleteTradingNameView

class DeleteTradingNameControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DeleteTradingNameFormProvider()
  private val form = formProvider()

  private val index = Index(0)
  private val tradingName = "foo"

  private lazy val deleteTradingNameRoute = routes.DeleteTradingNameController.onPageLoad(NormalMode, index).url
  private lazy val deleteTradingNameAmendRoute = routes.DeleteTradingNameController.onPageLoad(AmendMode, index).url

  private val baseUserAnswers = basicUserAnswersWithVatInfo.set(TradingNamePage(index), tradingName).success.value

  "DeleteTradingName Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteTradingNameRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteTradingNameView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, index, tradingName)(request, messages(application)).toString
      }
    }

    "must delete a record and redirect to the next page when the user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteTradingNameRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers.remove(TradingNamePage(index)).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeleteTradingNamePage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must not delete a record and redirect to the next page when the user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteTradingNameRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeleteTradingNamePage(index).navigate(NormalMode, baseUserAnswers).url
        verify(mockSessionRepository, never()).set(eqTo(baseUserAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteTradingNameRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteTradingNameView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, index, tradingName)(request, messages(application)).toString
      }
    }

    "must redirect to CheckYourAnswers for a GET if the trading name is not found" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, deleteTradingNameRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "in AmendMode" - {

      "must redirect to resolve missing answers for a GET if the trading name is not found" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, deleteTradingNameAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }
    }
  }
}

