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
import cats.data.Validated.Valid
import connectors.RegistrationConnector
import controllers.amend.routes as amendRoutes
import models.Country
import models.euDetails.{EuConsumerSalesMethod, EuDetails, RegistrationType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import pages.euDetails.*
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import queries.AllEuDetailsQuery
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RegistrationValidationService}
import testutils.RegistrationData
import viewmodels.DeleteAllFixedEstablishmentsAsPartOfVatGroupViewModel
import viewmodels.govuk.SummaryListFluency
import views.html.DeleteAllFixedEstablishmentView

import scala.concurrent.Future

class DeleteAllFixedEstablishmentControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val country = Country.euCountries.head
  private val euVatDetails = List(
    EuDetails(
      euCountry = country,
      sellsGoodsToEUConsumers = true,
      sellsGoodsToEUConsumerMethod = Some(EuConsumerSalesMethod.FixedEstablishment),
      registrationType = Some(RegistrationType.VatNumber),
      vatRegistered = Some(true),
      euVatNumber = None,
      euTaxReference = Some("12345678"),
      fixedEstablishmentTradingName = Some("Fixed Establishment trading name"),
      fixedEstablishmentAddress = None,
      euSendGoodsTradingName = None,
      euSendGoodsAddress = None))

  private val baseUserAnswers =
    basicUserAnswersWithVatInfo
      .set(TaxRegisteredInEuPage, true).success.value
      .set(AllEuDetailsQuery, euVatDetails).success.value

  private val registration = RegistrationData.registration

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockRegistrationValidationService = mock[RegistrationValidationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRegistrationService)
    Mockito.reset(mockRegistrationValidationService)
    Mockito.reset(mockAuthenticatedUserAnswersRepository)
  }

  "DeleteAllFixedEstablishment Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(Some(registration))
      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn Future.successful(baseUserAnswers)
      when(mockRegistrationValidationService.fromUserAnswers(any(), any())(any(), any(), any())) thenReturn Future.successful(Valid(registration))
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(baseUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RegistrationValidationService].toInstance(mockRegistrationValidationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.DeleteAllFixedEstablishmentController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[DeleteAllFixedEstablishmentView]
        val viewModel = DeleteAllFixedEstablishmentsAsPartOfVatGroupViewModel(euVatDetails)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(viewModel, false)(request, messages(application)).toString
      }
    }

    "must redirect to Not Registered Page when no registration found" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Future.successful(None)
      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Future.successful(Right(vatCustomerInfo))
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn Future.successful(true)

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, amendRoutes.StartAmendJourneyController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual controllers.routes.NotRegisteredController.onPageLoad().url
      }
    }
  }
}
