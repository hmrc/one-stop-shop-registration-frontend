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
import forms.CheckVatDetailsFormProvider
import models.iossRegistration.IossEtmpDisplayRegistration
import models.{CheckVatDetails, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.{CheckVatDetailsPage, HasTradingNamePage, RegisteredForOssInEuPage}
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.AllTradingNames
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import viewmodels.CheckVatDetailsViewModel
import views.html.CheckVatDetailsView

class CheckVatDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new CheckVatDetailsFormProvider()
  private val form = formProvider()

  private lazy val checkVatDetailsRoute = routes.CheckVatDetailsController.onPageLoad().url

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  "CheckVatDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, checkVatDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CheckVatDetailsView]
        implicit val msgs = messages(application)
        val viewModel = CheckVatDetailsViewModel(vrn, vatCustomerInfo)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, viewModel)(request, implicitly).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = basicUserAnswersWithVatInfo.set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, checkVatDetailsRoute)

        val view = application.injector.instanceOf[CheckVatDetailsView]
        implicit val msgs = messages(application)
        val viewModel = CheckVatDetailsViewModel(vrn, vatCustomerInfo)

        val result = route(application, request).value

        status(result) `mustBe` OK
        contentAsString(result) `mustBe`
          view(form.fill(CheckVatDetails.Yes), viewModel)(request, implicitly).toString
      }
    }

    "must redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository)
          )
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkVatDetailsRoute)
            .withFormUrlEncodedBody(("value", CheckVatDetails.Yes.toString))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswersWithVatInfo.set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` CheckVatDetailsPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must redirect to the next page when valid data is submitted when an IOSS Registration is present" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(
          userAnswers = Some(basicUserAnswersWithVatInfo),
          iossNumber = Some(iossNumber),
          iossEtmpDisplayRegistration = Some(iossEtmpDisplayRegistration)
        )
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, checkVatDetailsRoute)
            .withFormUrlEncodedBody(("value", CheckVatDetails.Yes.toString))

        val result = route(application, request).value
        val expectedAnswers = basicUserAnswersWithVatInfo
          .set(CheckVatDetailsPage, CheckVatDetails.Yes).success.value
          .set(HasTradingNamePage, true).success.value
          .set(AllTradingNames, iossEtmpDisplayRegistration.tradingNames.map(_.tradingName).toList).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` CheckVatDetailsPage.navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request =
          FakeRequest(POST, checkVatDetailsRoute)
            .withFormUrlEncodedBody(("value", "invalid value"))

        val boundForm = form.bind(Map("value" -> "invalid value"))

        val view = application.injector.instanceOf[CheckVatDetailsView]
        implicit val msgs = messages(application)
        val viewModel = CheckVatDetailsViewModel(vrn, vatCustomerInfo)

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, viewModel)(request, implicitly).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, checkVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no VAT data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(RegisteredForOssInEuPage, false).success.value)).build()

      running(application) {
        val request = FakeRequest(GET, checkVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, checkVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER

        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "redirect to Journey Recovery for a POST if no VAT data is found" in {

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers.set(RegisteredForOssInEuPage, false).success.value)).build()

      running(application) {
        val request =
          FakeRequest(POST, checkVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER

        redirectLocation(result).value `mustBe` routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
