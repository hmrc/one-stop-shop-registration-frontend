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

package controllers

import base.SpecBase
import controllers.amend.{routes => amendRoutes}
import models.euDetails.{EuConsumerSalesMethod, RegistrationType}
import models.{AmendMode, Country, Index, InternationalAddress, NormalMode, UserAnswers}
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchersSugar.eqTo
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.euDetails._
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import queries.EuDetailsQuery
import repositories.AuthenticatedUserAnswersRepository
import views.html.FixedEstablishmentVRNAlreadyRegisteredView

import scala.concurrent.Future

class FixedEstablishmentVRNAlreadyRegisteredControllerSpec extends SpecBase {

  private val countryIndex: Index = Index(0)

  private val answers: UserAnswers = basicUserAnswersWithVatInfo
    .set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(countryIndex), Country("FR", "France")).success.value
    .set(SellsGoodsToEUConsumersPage(countryIndex), true).success.value
    .set(SellsGoodsToEUConsumerMethodPage(countryIndex), EuConsumerSalesMethod.DispatchWarehouse).success.value
    .set(RegistrationTypePage(countryIndex), RegistrationType.VatNumber).success.value
    .set(EuVatNumberPage(countryIndex), "FR123456789").success.value
    .set(EuSendGoodsTradingNamePage(countryIndex), "French trading name").success.value
    .set(EuSendGoodsAddressPage(countryIndex), InternationalAddress("Line 1", None, "Town", None, None, Country("FR", "France"))).success.value

  "FixedEstablishmentVRNAlreadyRegistered Controller" - {

    ".onPageLoad" - {

      "must return OK and the correct view for a GET in NormalMode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

        running(application) {
          val request = FakeRequest(GET, routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(NormalMode, countryIndex).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FixedEstablishmentVRNAlreadyRegisteredView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(NormalMode, countryIndex)(request, messages(application)).toString
        }
      }

      "must return OK and the correct view for a GET in AmendMode" in {

        val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), mode = Some(AmendMode)).build()

        running(application) {
          val request = FakeRequest(GET, routes.FixedEstablishmentVRNAlreadyRegisteredController.onPageLoad(AmendMode, countryIndex).url)

          val result = route(application, request).value

          val view = application.injector.instanceOf[FixedEstablishmentVRNAlreadyRegisteredView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(AmendMode, countryIndex)(request, messages(application)).toString
        }
      }

    }

    ".deleteAndRedirect" - {

      "must delete the scheme for the country and redirect to Change Your Registration in Amend mode" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = Some(answers))
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, routes.FixedEstablishmentVRNAlreadyRegisteredController.deleteAndRedirect(countryIndex).url)

          val result = route(application, request).value

          val expectedAnswers = answers.remove(EuDetailsQuery(countryIndex)).success.value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe amendRoutes.ChangeYourRegistrationController.onPageLoad().url
          verify(mockSessionRepository, times(1)).set(eqTo(expectedAnswers))

        }
      }

      "must redirect to Amend Journey Recovery if no answers present" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.set(any())) thenReturn Future.successful(true)

        val application = applicationBuilder(userAnswers = None)
          .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
          .build()

        running(application) {
          val request = FakeRequest(GET, controllers.routes.FixedEstablishmentVRNAlreadyRegisteredController.deleteAndRedirect(countryIndex).url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustBe amendRoutes.AmendJourneyRecoveryController.onPageLoad().url
          verifyNoInteractions(mockSessionRepository)

        }
      }

    }

  }
}
