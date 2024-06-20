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

package controllers.rejoin

import base.SpecBase
import connectors.RegistrationConnector
import models.RejoinMode
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import services.{RegistrationService, RejoinRegistrationService}
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps
import viewmodels.govuk.SummaryListFluency


class StartRejoinJourneyControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach{

  private val registration = RegistrationData.registration

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockRejoinRegistrationService = mock[RejoinRegistrationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]


  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRegistrationService)
    Mockito.reset(mockRejoinRegistrationService)
    Mockito.reset(mockAuthenticatedUserAnswersRepository)
  }

  "StartRejoinJourney Controller" - {

    "must redirect to Has Already Made Sales when a registration has been successfully retrieved and it passes exclusion checks" in {

      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(registration).toFuture
      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value


        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.HasMadeSalesController.onPageLoad(RejoinMode).url
      }
    }

    "must redirect to Not Registered Page when no registration found" in {
      when(mockRegistrationConnector.getRegistration()(any())) thenReturn Some(registration).toFuture
      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn false
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value


        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe controllers.rejoin.routes.CannotRejoinController.onPageLoad().url
      }
    }
  }
}
