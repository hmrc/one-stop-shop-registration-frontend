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
import controllers.routes
import controllers.euDetails.{routes => euDetailsRoutes}
import controllers.amend.{routes => amendRoutes}
import forms.euDetails.EuSendGoodsAddressFormProvider
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.{AmendMode, Country, Index, InternationalAddress, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import views.html.euDetails.EuSendGoodsAddressView

import scala.concurrent.Future

class EuSendGoodsAddressControllerSpec extends SpecBase with MockitoSugar {

  private val country: Country = Country("FR", "France")
  private val index: Index = Index(0)
  private val businessName: String = "Business Name"

  private val exampleAddress: InternationalAddress = InternationalAddress("line1", None, "town", None, None, country)


  private val formProvider = new EuSendGoodsAddressFormProvider()
  private val form = formProvider(country)

  private lazy val euSendGoodsAddressRoute = euDetailsRoutes.EuSendGoodsAddressController.onPageLoad(NormalMode, index).url
  private lazy val euSendGoodsAddressAmendRoute = euDetailsRoutes.EuSendGoodsAddressController.onPageLoad(AmendMode, index).url

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo.set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(index), country).success.value
      .set(SellsGoodsToEUConsumersPage(index), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(index), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(RegistrationTypePage(index), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(index), "123456778").success.value
    .set(EuSendGoodsTradingNamePage(index), businessName).success.value

  "EuSendGoodsAddress Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsAddressRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[EuSendGoodsAddressView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, NormalMode, index, businessName, country)(request, messages(application)).toString
      }
    }

    "must populate the view correctly on a GET when the question has previously been answered" in {

      val userAnswers = baseUserAnswers.set(EuSendGoodsAddressPage(index), exampleAddress).success.value

      val application = applicationBuilder(userAnswers = Some(userAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsAddressRoute)

        val view = application.injector.instanceOf[EuSendGoodsAddressView]

        val result = route(application, request).value

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(exampleAddress), NormalMode, index, businessName, country)(request, messages(application)).toString
      }
    }

    "must save the answer and redirect to the next page when valid data is submitted" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsAddressRoute)
            .withFormUrlEncodedBody(("line1", exampleAddress.line1), ("country", exampleAddress.country.code),
              ("townOrCity", exampleAddress.townOrCity)
            )

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers
          .set(EuSendGoodsAddressPage(index), exampleAddress).success.value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual EuSendGoodsAddressPage(index).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsAddressRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[EuSendGoodsAddressView]

        val result = route(application, request).value

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(boundForm, NormalMode, index, businessName, country)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsAddressRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Amend Journey Recovery for a GET if no existing data is found in AmendMode" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request = FakeRequest(GET, euSendGoodsAddressAmendRoute)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a POST if no existing data is found" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsAddressRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must redirect to Amend Journey Recovery for a POST if no existing data is found in AmendMode" in {

      val application = applicationBuilder(userAnswers = None).build()

      running(application) {
        val request =
          FakeRequest(POST, euSendGoodsAddressAmendRoute)
            .withFormUrlEncodedBody(("value", "answer"))

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
