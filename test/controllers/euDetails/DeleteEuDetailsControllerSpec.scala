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
import controllers.amend.routes as amendRoutes
import controllers.euDetails.routes as euDetailsRoutes
import controllers.routes
import forms.euDetails.DeleteEuDetailsFormProvider
import models.euDetails.{EuConsumerSalesMethod, EuDetails, RegistrationType}
import models.{AmendMode, Country, Index, NormalMode}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito.{never, times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.EuDetailsQuery
import repositories.AuthenticatedUserAnswersRepository
import utils.FutureSyntax.FutureOps
import views.html.euDetails.DeleteEuDetailsView

class DeleteEuDetailsControllerSpec extends SpecBase with MockitoSugar {

  private val countryIndex = Index(0)
  private val country = Country.euCountries.head
  private val euVatDetails =
    EuDetails(
      country, sellsGoodsToEUConsumers = true, Some(EuConsumerSalesMethod.DispatchWarehouse), Some(RegistrationType.TaxId), vatRegistered = Some(false), None, Some("12345678"), None, None, None, None)

  private lazy val deleteEuVatDetailsRoute = euDetailsRoutes.DeleteEuDetailsController.onPageLoad(NormalMode, countryIndex).url
  private lazy val deleteEuVatDetailsAmendRoute = euDetailsRoutes.DeleteEuDetailsController.onPageLoad(AmendMode, countryIndex).url

  private val formProvider = new DeleteEuDetailsFormProvider()
  private val form = formProvider(euVatDetails.euCountry.name)

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo
      .set(TaxRegisteredInEuPage, true).success.value
      .set(EuCountryPage(countryIndex), country).success.value
      .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
      .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
      .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
      .set(EuVatNumberPage(countryIndex), "12345678").success.value
      .set(EuSendGoodsTradingNamePage(countryIndex), "Foo").success.value
      .set(EuSendGoodsAddressPage(countryIndex), arbitraryInternationalAddress.arbitrary.sample.value).success.value

  "DeleteEuVatDetails Controller" - {

    "must return OK and the correct view for a GET" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, deleteEuVatDetailsRoute)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteEuDetailsView]

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(form, NormalMode, countryIndex, euVatDetails.euCountry.name)(request, messages(application)).toString
      }
    }

    "must delete a record and redirect to the next page when the user answers Yes" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", "true"))

        val result = route(application, request).value
        val expectedAnswers = baseUserAnswers.remove(EuDetailsQuery(Index(0))).success.value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeleteEuDetailsPage(Index(0)).navigate(NormalMode, expectedAnswers).url
        verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))
      }
    }

    "must not delete a record and redirect to the next page when the user answers No" in {

      val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

      when(mockSessionRepository.set(any())) thenReturn true.toFuture

      val application =
        applicationBuilder(userAnswers = Some(baseUserAnswers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

      running(application) {
        val request =
          FakeRequest(POST, deleteEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", "false"))

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` DeleteEuDetailsPage(Index(0)).navigate(NormalMode, baseUserAnswers).url
        verify(mockSessionRepository, never()).set(eqTo(baseUserAnswers))
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers)).build()

      running(application) {
        val request =
          FakeRequest(POST, deleteEuVatDetailsRoute)
            .withFormUrlEncodedBody(("value", ""))

        val boundForm = form.bind(Map("value" -> ""))

        val view = application.injector.instanceOf[DeleteEuDetailsView]

        val result = route(application, request).value

        status(result) `mustBe` BAD_REQUEST
        contentAsString(result) `mustBe` view(boundForm, NormalMode, countryIndex, euVatDetails.euCountry.name)(request, messages(application)).toString
      }
    }

    "must redirect to CheckYourAnswers for a GET if no EU VAT details exist" in {

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo)).build()

      running(application) {
        val request = FakeRequest(GET, deleteEuVatDetailsRoute)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value `mustBe` routes.CheckYourAnswersController.onPageLoad().url
      }
    }

    "in AmendMode" - {

      "must redirect to resolve missing answers for a GET if no EU VAT details exist" in {

        val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
          .build()

        running(application) {
          val request = FakeRequest(GET, deleteEuVatDetailsAmendRoute)

          val result = route(application, request).value

          status(result) `mustBe` SEE_OTHER
          redirectLocation(result).value `mustBe` amendRoutes.ChangeYourRegistrationController.onPageLoad().url
        }
      }
    }
  }
}
