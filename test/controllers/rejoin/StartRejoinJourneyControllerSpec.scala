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
import models.core.{Match, MatchType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import repositories.AuthenticatedUserAnswersRepository
import services.*
import testutils.RegistrationData
import utils.FutureSyntax.FutureOps
import viewmodels.govuk.SummaryListFluency

import java.time.LocalDate

class StartRejoinJourneyControllerSpec extends SpecBase with MockitoSugar with SummaryListFluency with BeforeAndAfterEach {

  private val registration = RegistrationData.registration
  private val futureVatCustomerInfo = vatCustomerInfo.copy(deregistrationDecisionDate = None)
  private val deregisteredVatCustomerInfo = vatCustomerInfo.copy(deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1)))

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "33333333",
    None,
    "DE",
    None,
    None,
    exclusionEffectiveDate = Some(LocalDate.now),
    None,
    None
  )

  private val mockRegistrationConnector = mock[RegistrationConnector]
  private val mockRegistrationService = mock[RegistrationService]
  private val mockRejoinRegistrationService = mock[RejoinRegistrationService]
  private val mockAuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]
  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockRejoinPreviousRegistrationValidationService = mock[RejoinPreviousRegistrationValidationService]
  private val mockRejoinEuRegistrationValidationService = mock[RejoinEuRegistrationValidationService]

  override def beforeEach(): Unit = {
    Mockito.reset(mockRegistrationConnector)
    Mockito.reset(mockRegistrationService)
    Mockito.reset(mockRejoinRegistrationService)
    Mockito.reset(mockAuthenticatedUserAnswersRepository)
    Mockito.reset(mockCoreRegistrationValidationService)
    Mockito.reset(mockRejoinPreviousRegistrationValidationService)
    Mockito.reset(mockRejoinEuRegistrationValidationService)
  }

  "StartRejoinJourney Controller" - {

    "must redirect to Has Already Made Sales when a registration has been successfully retrieved and it passes exclusion checks" in {

      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(futureVatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
      when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registration = Some(registration))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.HasMadeSalesController.onPageLoad(RejoinMode).url
      }
    }

    "must redirect to Not Registered Page when no registration found" in {

      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn false
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
      when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registration = Some(registration))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe controllers.rejoin.routes.CannotRejoinController.onPageLoad().url
      }
    }

    "must redirect to Ni Protocol Expired Page when singleMarketIndicator is false" in {

      val vatCustomerInfoNoSingleMarket = vatCustomerInfo.copy(singleMarketIndicator = Some(false))
      val userAnswersNoSingleMarket = basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatCustomerInfoNoSingleMarket))

      val application = applicationBuilder(userAnswers = Some(userAnswersNoSingleMarket)).build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe controllers.routes.NiProtocolExpiredController.onPageLoad().url
      }
    }

    "must redirect to Kick-out Page when Trader is Deregistered from VAT" in {

      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(deregisteredVatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
      when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registration = Some(registration))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe controllers.rejoin.routes.CannotRejoinController.onPageLoad().url
      }
    }

    "must redirect to the page returned by previous registration validation" in {

      val redirectPage = controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
        genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString)

      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
      when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
      when(mockRejoinPreviousRegistrationValidationService.validatePreviousRegistrations(any())(any(), any())) thenReturn Some(Redirect(redirectPage)).toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registration = Some(registration))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .overrides(bind[RejoinPreviousRegistrationValidationService].toInstance(mockRejoinPreviousRegistrationValidationService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe redirectPage.url
      }
    }

    "must redirect to the page returned by EU Registration validation" in {

      val redirectPage = controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(
        genericMatch.memberState)

      when(mockRegistrationConnector.getVatCustomerInfo()(any())) thenReturn Right(vatCustomerInfo).toFuture
      when(mockRegistrationService.toUserAnswers(any(), any(), any())) thenReturn completeUserAnswers.toFuture
      when(mockRejoinRegistrationService.canRejoinRegistration(any(), any())) thenReturn true
      when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
      when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
      when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
      when(mockRejoinPreviousRegistrationValidationService.validatePreviousRegistrations(any())(any(), any())) thenReturn None.toFuture
      when(mockRejoinEuRegistrationValidationService.validateEuRegistrations(any())(any(), any())) thenReturn Some(Redirect(redirectPage)).toFuture

      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers), registration = Some(registration))
        .overrides(bind[RegistrationConnector].toInstance(mockRegistrationConnector))
        .overrides(bind[RegistrationService].toInstance(mockRegistrationService))
        .overrides(bind[RejoinRegistrationService].toInstance(mockRejoinRegistrationService))
        .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository))
        .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
        .overrides(bind[RejoinPreviousRegistrationValidationService].toInstance(mockRejoinPreviousRegistrationValidationService))
        .overrides(bind[RejoinEuRegistrationValidationService].toInstance(mockRejoinEuRegistrationValidationService))
        .build()

      running(application) {
        val request = FakeRequest(GET, controllers.rejoin.routes.StartRejoinJourneyController.onPageLoad().url)

        val result = route(application, request).value

        status(result) `mustBe` SEE_OTHER
        redirectLocation(result).value mustBe redirectPage.url
      }
    }
  }
}
