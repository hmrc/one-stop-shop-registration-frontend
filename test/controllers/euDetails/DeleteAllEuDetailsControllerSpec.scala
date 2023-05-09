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
import controllers.euDetails.{routes => euRoutes}
import controllers.routes
import forms.euDetails.DeleteAllEuDetailsFormProvider
import models.{CheckMode, Country, Index, InternationalAddress}
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EuDetailsTopLevelNode
import repositories.AuthenticatedUserAnswersRepository
import views.html.euDetails.DeleteAllEuDetailsView

import scala.concurrent.Future

class DeleteAllEuDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val formProvider = new DeleteAllEuDetailsFormProvider()
  private val form = formProvider()

  private val userAnswers = basicUserAnswersWithVatInfo
    .set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(Index(0)), Country("FR", "France")).success.value
    .set(SellsGoodsToEUConsumersPage(Index(0)), true).success.value
    .set(SellsGoodsToEUConsumerMethodPage(Index(0)), EuConsumerSalesMethod.DispatchWarehouse).success.value
    .set(RegistrationTypePage(Index(0)), RegistrationType.VatNumber).success.value
    .set(EuVatNumberPage(Index(0)), "FR123456789").success.value
    .set(EuSendGoodsTradingNamePage(Index(0)), "French trading name").success.value
    .set(EuSendGoodsAddressPage(Index(0)), InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France"))).success.value
    .set(EuCountryPage(Index(1)), Country("DE", "Germany")).success.value
    .set(SellsGoodsToEUConsumersPage(Index(1)), false).success.value
    .set(VatRegisteredPage(Index(1)), true).success.value
    .set(EuVatNumberPage(Index(1)), "DE123456789").success.value

  private lazy val deleteAllEuDetailsRoute = euRoutes.DeleteAllEuDetailsController.onPageLoad().url

  "DeleteAllEuDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, deleteAllEuDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteAllEuDetailsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, CheckMode)(request, messages(application)).toString
      }
    }

    "must delete all eu details and redirect to the next page when user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAllEuDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = userAnswers
          .set(DeleteAllEuDetailsPage, true).success.value
          .remove(EuDetailsTopLevelNode).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual DeleteAllEuDetailsPage.navigate(CheckMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must not delete all eu details and redirect to the next page when user answers No" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(userAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAllEuDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value
        val expectedAnswers = userAnswers
          .set(DeleteAllEuDetailsPage, false).success.value
          .set(TaxRegisteredInEuPage, true).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual DeleteAllEuDetailsPage.navigate(CheckMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAllEuDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteAllEuDetailsView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, CheckMode)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, deleteAllEuDetailsRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteAllEuDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
