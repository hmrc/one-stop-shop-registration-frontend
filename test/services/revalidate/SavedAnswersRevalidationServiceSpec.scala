/*
 * Copyright 2026 HM Revenue & Customs
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

package services.revalidate

import base.SpecBase
import controllers.revalidation.routes
import models.PreviousScheme.{IOSSWI, OSSNU, OSSU}
import models.core.{Match, TraderId}
import models.domain.{PreviousSchemeNumbers, VatCustomerInfo}
import models.euDetails.EuConsumerSalesMethod.DispatchWarehouse
import models.euDetails.EuOptionalDetails
import models.euDetails.RegistrationType.{TaxId, VatNumber}
import models.exclusions.ExclusionReason
import models.exclusions.ExclusionReason.FailsToComply
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import models.revalidate.ActiveTraderResult
import models.{Country, Index, PreviousScheme, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.{BeforeAndAfterEach, PrivateMethodTester}
import org.scalatestplus.mockito.MockitoSugar.mock
import pages.euDetails.*
import pages.previousRegistrations.{PreviousEuCountryPage, PreviousOssNumberPage, PreviousSchemePage, PreviouslyRegisteredPage}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import queries.ActiveTraderResultQuery
import repositories.AuthenticatedUserAnswersRepository
import services.CoreRegistrationValidationService
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SavedAnswersRevalidationServiceSpec extends SpecBase with PrivateMethodTester with BeforeAndAfterEach {

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val mockCoreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockAuthenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  private val previousEuCountry1: Country = arbitraryCountry.arbitrary.sample.value
  private val previousEuCountry2: Country = arbitraryCountry.arbitrary.retryUntil(_.code != previousEuCountry1.code).sample.value
  private val previousSchemeDetails1: SchemeDetailsWithOptionalVatNumber = arbitrarySchemeDetailsWithOptionalVatNumber.arbitrary.sample.value
    .copy(previousScheme = Some(IOSSWI))

  private val previousSchemeDetails2: SchemeDetailsWithOptionalVatNumber = previousSchemeDetails1
    .copy(
      previousScheme = Some(OSSU),
      previousSchemeNumbers = Some(arbitrarySchemeNumbersWithOptionalVatNumber.arbitrary.sample.value)
    )

  private val previousSchemeDetails3: SchemeDetailsWithOptionalVatNumber = previousSchemeDetails2
    .copy(
      previousScheme = Some(OSSNU),
      previousSchemeNumbers = Some(arbitrarySchemeNumbersWithOptionalVatNumber.arbitrary.sample.value)
    )

  private val previousRegistration1: PreviousRegistrationDetailsWithOptionalVatNumber = PreviousRegistrationDetailsWithOptionalVatNumber(
    previousEuCountry = previousEuCountry1,
    previousSchemesDetails = Some(List(previousSchemeDetails1, previousSchemeDetails2, previousSchemeDetails3))
  )

  private val previousRegistration2: PreviousRegistrationDetailsWithOptionalVatNumber = previousRegistration1
    .copy(previousEuCountry = previousEuCountry2)

  private val allPreviousRegistrations: List[PreviousRegistrationDetailsWithOptionalVatNumber] = List(previousRegistration1, previousRegistration2)

  private val intermediaryNumber: String = genIntermediaryNumber.sample.value

  private val aMatch: Match = arbitraryMatch.arbitrary.sample.value
    .copy(intermediary = Some(intermediaryNumber))

  private def index(indexVal: Int): Index = {
    Index(indexVal)
  }

  private val baseEuDetails: EuOptionalDetails = arbitraryEuOptionalDetails.arbitrary.sample.value.copy(
    sellsGoodsToEUConsumers = Some(true),
    sellsGoodsToEUConsumerMethod = Some(DispatchWarehouse),
    registrationType = Some(VatNumber),
    vatRegistered = Some(true)
  )

  private val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
    .set(TaxRegisteredInEuPage, true).success.value
    .set(EuCountryPage(index(0)), baseEuDetails.euCountry).success.value
    .set(SellsGoodsToEUConsumersPage(index(0)), baseEuDetails.sellsGoodsToEUConsumers.value).success.value
    .set(SellsGoodsToEUConsumerMethodPage(index(0)), baseEuDetails.sellsGoodsToEUConsumerMethod.value).success.value
    .set(RegistrationTypePage(index(0)), baseEuDetails.registrationType.value).success.value
    .set(EuVatNumberPage(index(0)), baseEuDetails.euVatNumber.head).success.value
    .set(EuSendGoodsTradingNamePage(index(0)), baseEuDetails.euSendGoodsTradingName.head).success.value
    .set(EuSendGoodsAddressPage(index(0)), baseEuDetails.euSendGoodsAddress.head).success.value
  
  override def beforeEach(): Unit = {
    Mockito.reset(
      mockCoreRegistrationValidationService,
      mockAuthenticatedUserAnswersRepository
    )
  }

  "SavedAnswersRevalidationService" - {

    ".revalidateSavedUserAnswers" - {

      "when re-validating UK VRN" - {

        "must return None when UK VRN is not already registered, expired or quarantined" in {

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service.revalidateSavedUserAnswers().futureValue

          result `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verifyNoMoreInteractions(mockCoreRegistrationValidationService)
        }

        "must redirect to the corresponding URL when UK VRN is already registered" in {

          val activeMatch: Match = arbitraryMatch.arbitrary.sample.value

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(ActiveTraderResultQuery, activeTrader).success.value

          val result = service.revalidateSavedUserAnswers().futureValue

          result `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verifyNoMoreInteractions(mockCoreRegistrationValidationService)
        }

        "must redirect to the corresponding URL when UK VRN is expired" in {

          val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
            deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
          )

          val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo.copy(
            vatInfo = Some(expiredVrnVatInfo)
          )

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers().futureValue

          result `mustBe` Some(Redirect(routes.RevalidateVrnExpiredController.onPageLoad().url))
          verifyNoInteractions(mockCoreRegistrationValidationService)
          verifyNoMoreInteractions(mockCoreRegistrationValidationService)
        }
      }

      "must return None when neither previous registrations or EU details are present" in {

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val result = service.revalidateSavedUserAnswers().futureValue

        result `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
        verifyNoMoreInteractions(mockCoreRegistrationValidationService)
      }

      "when re-validating previous registrations" - {

        "must return None when previous registrations are present but no active match is found" in {

          val previousSchemeNumber: String = previousSchemeDetails1.previousSchemeNumbers.value.previousSchemeNumber.value

          val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), previousRegistration1.previousEuCountry).success.value
            .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails1.previousScheme.value).success.value
            .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(previousSchemeDetails1.previousScheme.value),
            eqTo(None),
            eqTo(previousRegistration1.previousEuCountry.code),
          )(any(), any())
        }

        "must return a result when there are previous registrations present and an active match is found" in {

          val previousSchemeNumber: String = previousSchemeDetails2.previousSchemeNumbers.value.previousSchemeNumber.value

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = previousSchemeNumber),
            memberState = previousEuCountry1.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), previousRegistration1.previousEuCountry).success.value
            .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails2.previousScheme.value).success.value
            .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          val expectedAnswers: UserAnswers = updatedUserAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(previousSchemeDetails2.previousScheme.value),
            eqTo(None),
            eqTo(previousRegistration1.previousEuCountry.code),
          )(any(), any())
        }

        "must return a result when there are previous registrations present and a quarantined trader is found" in {

          val previousSchemeNumber: String = previousSchemeDetails1.previousSchemeNumbers.value.previousSchemeNumber.value

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = previousSchemeNumber),
            memberState = previousEuCountry1.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(PreviouslyRegisteredPage, true).success.value
            .set(PreviousEuCountryPage(Index(0)), previousRegistration1.previousEuCountry).success.value
            .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails1.previousScheme.value).success.value
            .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          ).url))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(previousSchemeDetails1.previousScheme.value),
            eqTo(None),
            eqTo(previousRegistration1.previousEuCountry.code),
          )(any(), any())
        }
      }

      "when re-validating EU details" - {

        "must return None when EU details are present but no active match is found" in {

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when EU details are present and an active match is found" in {

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          val expectedAnswers: UserAnswers = updatedUserAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when EU details are present and a quarantined trader is found" in {

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val result = service.revalidateSavedUserAnswers()

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          ).url))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }
      }
    }

    ".checkEuDetails" - {

      "must return None when there are no EuDetails to revalidate" in {

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verifyNoInteractions(mockCoreRegistrationValidationService)
      }

      "must return None when EuDetails are present but no active match is found" in {

        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
      }

      "with complete answers" - {

        "must redirect to the corresponding URL when EuDetails are present and an active match is found" in {

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

          val result = service invokePrivate privateMethodCall(hc, request)

          val expectedAnswers: UserAnswers = updatedUserAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when EuDetails are present and a quarantined trader is found" in {

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

          val result = service invokePrivate privateMethodCall(hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          )))
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }
      }

      "with incomplete answers" - {

        val incompleteEuDetails: UserAnswers = updatedUserAnswers
          .remove(EuSendGoodsTradingNamePage(index(0))).success.value
          .remove(EuSendGoodsAddressPage(index(0))).success.value

        "must redirect to the corresponding URL when EuDetails are present and an active match is found" in {

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, incompleteEuDetails, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

          val result = service invokePrivate privateMethodCall(hc, request)

          val expectedAnswers: UserAnswers = incompleteEuDetails
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when EuDetails are present and a quarantined trader is found" in {

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, incompleteEuDetails, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkEuDetails"))

          val result = service invokePrivate privateMethodCall(hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          )))
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }
      }
    }

    ".checkAllEuDetails" - {

      val euDetailsList: List[EuOptionalDetails] = List(baseEuDetails, arbitraryEuOptionalDetails.arbitrary.sample.value)

      val userAnswers: UserAnswers = updatedUserAnswers
        .set(EuCountryPage(index(1)), euDetailsList.head.euCountry).success.value
        .set(SellsGoodsToEUConsumersPage(index(1)), true).success.value
        .set(SellsGoodsToEUConsumerMethodPage(index(1)), DispatchWarehouse).success.value
        .set(RegistrationTypePage(index(1)), VatNumber).success.value
        .set(EuVatNumberPage(index(1)), euDetailsList.head.euVatNumber.head).success.value
        .set(EuSendGoodsTradingNamePage(index(1)), euDetailsList.head.euSendGoodsTradingName.head).success.value
        .set(EuSendGoodsAddressPage(index(1)), euDetailsList.head.euSendGoodsAddress.head).success.value

      "must return None when no match is found" in {

        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkAllEuDetails"))

        val result = service invokePrivate privateMethodCall(euDetailsList, hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euDetailsList.head.euVatNumber.head), eqTo(euDetailsList.head.euCountry.code), eqTo(!euDetailsList.head.sellsGoodsToEUConsumers.value))(any(), any())
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(!euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())
      }

      "must redirect to the corresponding URL when an active trader is found" in {

        val activeMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euDetailsList.tail.head.euVatNumber.head),
          memberState = euDetailsList.tail.head.euCountry.code,
          exclusionStatusCode = None,
          exclusionEffectiveDate = None
        )

        val activeTrader = ActiveTraderResult(
          isReversal = false,
          exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(!euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())) thenReturn Some(activeMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkAllEuDetails"))

        val result = service invokePrivate privateMethodCall(euDetailsList, hc, request)

        val expectedAnswers: UserAnswers = userAnswers
          .set(ActiveTraderResultQuery, activeTrader).success.value

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(!euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())
      }

      "must redirect to the corresponding URL when a quarantined trader is found" in {

        val quarantinedMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euDetailsList.tail.head.euVatNumber.head),
          memberState = euDetailsList.tail.head.euCountry.code,
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
        )

        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(!euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())) thenReturn Some(quarantinedMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkAllEuDetails"))

        val result = service invokePrivate privateMethodCall(euDetailsList, hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = quarantinedMatch.getEffectiveDate
        )))
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euDetailsList.tail.head.euVatNumber.head), eqTo(euDetailsList.tail.head.euCountry.code), eqTo(!euDetailsList.tail.head.sellsGoodsToEUConsumers.value))(any(), any())
      }
    }

    ".revalidateEuDetails" - {

      "when a EuVrn is present" - {

        "must return None when no match is found" in {

          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(baseEuDetails, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when an active trader is found" in {

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(baseEuDetails, hc, request)

          val expectedAnswers: UserAnswers = updatedUserAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }

        "must redirect to the corresponding URL when a quarantined trader is found" in {

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = baseEuDetails.euVatNumber.head),
            memberState = baseEuDetails.euCountry.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(baseEuDetails, hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          )))
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(baseEuDetails.euVatNumber.head), eqTo(baseEuDetails.euCountry.code), eqTo(!baseEuDetails.sellsGoodsToEUConsumers.value))(any(), any())
        }
      }

      "when a Tax reference is present" - {

        val euDetails: EuOptionalDetails = baseEuDetails.copy(
          registrationType = Some(TaxId),
          euVatNumber = None
        )

        val userAnswers: UserAnswers = updatedUserAnswers
          .remove(EuVatNumberPage(index(0))).success.value
          .set(RegistrationTypePage(index(0)), TaxId).success.value
          .set(EuTaxReferencePage(index(0)), euDetails.euTaxReference.head).success.value

        "must return None when no match is found" in {

          when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(euDetails, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euDetails.euTaxReference.head), eqTo(euDetails.euCountry.code))(any(), any())
        }

        "must redirect to the corresponding URL when an active trader is found" in {

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = euDetails.euTaxReference.head),
            memberState = euDetails.euCountry.code,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(euDetails, hc, request)

          val expectedAnswers: UserAnswers = userAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euDetails.euTaxReference.head), eqTo(euDetails.euCountry.code))(any(), any())
        }

        "must redirect to the corresponding URL when a quarantined trader is found" in {

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = euDetails.euTaxReference.head),
            memberState = euDetails.euCountry.code,
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, userAnswers, None, 0, None)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuDetails"))

          val result = service invokePrivate privateMethodCall(euDetails, hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          )))
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euDetails.euTaxReference.head), eqTo(euDetails.euCountry.code))(any(), any())
        }
      }
    }

    ".revalidateEuTaxReference" - {

      val euTaxIdentifier: String = arbitrary[String].sample.value
      val countryCode: String = arbitraryCountry.arbitrary.sample.value.code

      "must return None if no active or quarantined trader is found" in {

        when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuTaxReference"))

        val result = service invokePrivate privateMethodCall(euTaxIdentifier, countryCode, hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euTaxIdentifier), eqTo(countryCode))(any(), any())
      }

      "must redirect to the corresponding URL when an active trader is found" in {

        val activeMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euTaxIdentifier),
          memberState = countryCode,
          exclusionStatusCode = None,
          exclusionEffectiveDate = None
        )

        val activeTraderResult: ActiveTraderResult = ActiveTraderResult(
          isReversal = false,
          exclusionEffectiveDate = None
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuTaxReference"))

        val result = service invokePrivate privateMethodCall(euTaxIdentifier, countryCode, hc, request)

        val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(ActiveTraderResultQuery, activeTraderResult).success.value

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euTaxIdentifier), eqTo(countryCode))(any(), any())
      }

      "must redirect to the corresponding URL when a quarantined trader is found" in {

        val quarantinedMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euTaxIdentifier),
          memberState = countryCode,
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuTaxReference"))

        val result = service invokePrivate privateMethodCall(euTaxIdentifier, countryCode, hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = quarantinedMatch.getEffectiveDate
        )))
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchEuTaxId(eqTo(euTaxIdentifier), eqTo(countryCode))(any(), any())
      }
    }

    ".revalidateEuVrn" - {

      val euVrn: String = arbitraryEuVatNumber.sample.value
      val countryCode: String = arbitraryCountry.arbitrary.sample.value.code

      "must return None if no active or quarantined trader is found" in {

        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuVrn"))

        val result = service invokePrivate privateMethodCall(euVrn, countryCode, true, hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euVrn), eqTo(countryCode), eqTo(true))(any(), any())
      }

      "must redirect to the corresponding URL when an active trader is found" in {

        val activeMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euVrn),
          memberState = countryCode,
          exclusionStatusCode = None,
          exclusionEffectiveDate = None
        )

        val activeTraderResult: ActiveTraderResult = ActiveTraderResult(
          isReversal = false,
          exclusionEffectiveDate = None
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuVrn"))

        val result = service invokePrivate privateMethodCall(euVrn, countryCode, true, hc, request)

        val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(ActiveTraderResultQuery, activeTraderResult).success.value

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euVrn), eqTo(countryCode), eqTo(true))(any(), any())
      }

      "must redirect to the corresponding URL when a quarantined trader is found" in {

        val quarantinedMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = euVrn),
          memberState = countryCode,
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateEuVrn"))

        val result = service invokePrivate privateMethodCall(euVrn, countryCode, true, hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = quarantinedMatch.getEffectiveDate
        )))
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchEuVrn(eqTo(euVrn), eqTo(countryCode), eqTo(true))(any(), any())
      }
    }

    ".checkPreviousRegistrations" - {

      "must return None when there are no previous registrations present" in {

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkPreviousRegistrations"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
      }

      "must return None when there are previous registrations present and no active match is found" in {

        val previousSchemeNumber: String = previousSchemeDetails1.previousSchemeNumbers.value.previousSchemeNumber.value

        val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(PreviouslyRegisteredPage, true).success.value
          .set(PreviousEuCountryPage(Index(0)), previousRegistration1.previousEuCountry).success.value
          .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails1.previousScheme.value).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkPreviousRegistrations"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(previousSchemeNumber),
          eqTo(previousSchemeDetails1.previousScheme.value),
          eqTo(None),
          eqTo(previousRegistration1.previousEuCountry.code)
        )(any(), any())
      }

      "must return None when there are previous registrations present and an active match is found and previous scheme is OSSNU" in {

        val previousSchemeNumber: String = previousSchemeDetails1.previousSchemeNumbers.value.previousSchemeNumber.value

        val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(PreviouslyRegisteredPage, true).success.value
          .set(PreviousEuCountryPage(Index(0)), previousEuCountry1).success.value
          .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails1.previousScheme.value).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

        val activeMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = previousSchemeNumber),
          memberState = previousEuCountry1.code,
          exclusionStatusCode = None,
          exclusionEffectiveDate = None
        )

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkPreviousRegistrations"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(previousSchemeNumber),
          eqTo(previousSchemeDetails1.previousScheme.value),
          eqTo(None),
          eqTo(previousEuCountry1.code)
        )(any(), any())
      }

      "must return a result when there are previous registrations present and an active match is found and previous scheme is OSSU" in {

        val previousSchemeNumber: String = previousSchemeDetails2.previousSchemeNumbers.value.previousSchemeNumber.value

        val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(PreviouslyRegisteredPage, true).success.value
          .set(PreviousEuCountryPage(Index(0)), previousEuCountry1).success.value
          .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails2.previousScheme.value).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

        val activeMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = previousSchemeNumber),
          memberState = previousEuCountry1.code,
          exclusionStatusCode = None,
          exclusionEffectiveDate = None
        )

        val activeTrader = ActiveTraderResult(
          isReversal = false,
          exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkPreviousRegistrations"))

        val result = service invokePrivate privateMethodCall(hc, request)

        val expectedAnswers: UserAnswers = updatedUserAnswers
          .set(ActiveTraderResultQuery, activeTrader).success.value

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(previousSchemeNumber),
          eqTo(previousSchemeDetails2.previousScheme.value),
          eqTo(None),
          eqTo(previousEuCountry1.code)
        )(any(), any())
      }

      "must return a result when there are previous registrations present and a quarantined match is found" in {

        val previousSchemeNumber: String = previousSchemeDetails1.previousSchemeNumbers.value.previousSchemeNumber.value

        val updatedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
          .set(PreviouslyRegisteredPage, true).success.value
          .set(PreviousEuCountryPage(Index(0)), previousEuCountry1).success.value
          .set(PreviousSchemePage(Index(0), Index(0)), previousSchemeDetails1.previousScheme.value).success.value
          .set(PreviousOssNumberPage(Index(0), Index(0)), PreviousSchemeNumbers(previousSchemeNumber, None)).success.value

        val quarantinedMatch: Match = aMatch.copy(
          traderId = TraderId(traderId = previousSchemeNumber),
          memberState = previousEuCountry1.code,
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("checkPreviousRegistrations"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = quarantinedMatch.getEffectiveDate
        )))
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(previousSchemeNumber),
          eqTo(previousSchemeDetails1.previousScheme.value),
          eqTo(None),
          eqTo(previousEuCountry1.code)
        )(any(), any())
      }
    }

    ".revalidatePreviousSchemeDetails" - {

      val countryCode: String = arbitraryCountry.arbitrary.sample.value.code

      val allPreviousSchemeDetails: List[SchemeDetailsWithOptionalVatNumber] = List(previousSchemeDetails1, previousSchemeDetails2, previousSchemeDetails3)

      "must return None when no active matches are found" in {

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(allPreviousSchemeDetails.head.previousSchemeNumbers.head.previousSchemeNumber.value),
          eqTo(allPreviousSchemeDetails.head.previousScheme.value),
          any(),
          eqTo(countryCode)
        )(any(), any())
      }

      "must continue to iterate through the list when optional scheme number values are missing and then return None when no active matches are found" in {

        val previousSchemeDetailsWithMissingVatNumber: SchemeDetailsWithOptionalVatNumber = previousSchemeDetails1.copy(
          previousSchemeNumbers = None
        )

        val updatedAllPreviousSchemeDetails: List[SchemeDetailsWithOptionalVatNumber] = List(previousSchemeDetailsWithMissingVatNumber, previousSchemeDetails2, previousSchemeDetails3)

        when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val result = service invokePrivate privateMethodCall(countryCode, updatedAllPreviousSchemeDetails, hc, request)

        result.futureValue `mustBe` None
        verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
          eqTo(allPreviousSchemeDetails.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value),
          eqTo(allPreviousSchemeDetails.tail.head.previousScheme.value),
          any(),
          eqTo(countryCode)
        )(any(), any())
      }

      "when it is an OSS scheme" - {

        "must return None when no active match is found" in {

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(3)).searchScheme(
            any(),
            any(),
            any(),
            eqTo(countryCode)
          )(any(), any())
        }

        "must return None when an active match is found for a OSSNU previous scheme" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.tail.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = previousSchemeNumber),
            memberState = countryCode,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.tail.tail.head.previousScheme.value),
            any(),
            any()
          )(any(), any())
          ) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` None
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
          verify(mockCoreRegistrationValidationService, times(3)).searchScheme(
            any(),
            any(),
            any(),
            eqTo(countryCode)
          )(any(), any())
        }

        "must save the active trader result and redirect to the corresponding URL when an active match is found and is a OSSU previous scheme" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = previousSchemeNumber),
            memberState = countryCode,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.tail.head.previousScheme.value),
            any(),
            any()
          )(any(), any())
          ) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(ActiveTraderResultQuery, activeTrader).success.value

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(2)).searchScheme(
            any(),
            any(),
            any(),
            eqTo(countryCode)
          )(any(), any())
        }

        "must redirect to the corresponding URL when a quarantined match is found" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = previousSchemeNumber),
            memberState = countryCode,
            exclusionStatusCode = Some(ExclusionReason.FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.tail.head.previousScheme.value),
            any(),
            any()
          )(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          ).url))
          verify(mockCoreRegistrationValidationService, times(2)).searchScheme(any(), any(), any(), eqTo(countryCode))(any(), any())
        }
      }

      "when it is an IOSS scheme" - {

        "must return None when no active match is found" in {

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(3)).searchScheme(any(), any(), any(), any())(any(), any())
        }

        "must return None when an active match is found" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
            memberState = countryCode,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.head.previousScheme.value),
            eqTo(Some(intermediaryNumber)),
            eqTo(countryCode)
          )(any(), any())
        }

        "must redirect to the corresponding URL when a quarantined match is found" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val quarantinedMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
            memberState = countryCode,
            exclusionStatusCode = Some(ExclusionReason.FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
          )

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails, hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          ).url))
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.head.previousScheme.value),
            eqTo(Some(intermediaryNumber)),
            eqTo(countryCode)
          )(any(), any())
        }
      }
    }

    ".revalidateAllPreviousRegistrations" - {

      "must iterate through all existing previous registrations and any encompassing previous scheme details" - {

        "and return None when no active matches are found" in {

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(allPreviousRegistrations, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(6)).searchScheme(any(), any(), any(), any())(any(), any())
        }

        "and continue to iterate through the list when optional scheme details are missing and return None when no active matches are found" in {

          val previousRegistrationWithoutOptionalSchemeDetails: PreviousRegistrationDetailsWithOptionalVatNumber = previousRegistration1
            .copy(previousSchemesDetails = None)

          val updatedAllPreviousRegistrations: List[PreviousRegistrationDetailsWithOptionalVatNumber] = List(previousRegistrationWithoutOptionalSchemeDetails, previousRegistration2)

          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service invokePrivate privateMethodCall(updatedAllPreviousRegistrations, hc, request)

          result.futureValue `mustBe` None
          verify(mockCoreRegistrationValidationService, times(3)).searchScheme(any(), any(), any(), any())(any(), any())
        }

        "when it is an OSS scheme" - {

          "and return None when no active match is found" in {

            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

            val result = service invokePrivate privateMethodCall(allPreviousRegistrations, hc, request)

            result.futureValue `mustBe` None
            verify(mockCoreRegistrationValidationService, times(6)).searchScheme(any(), any(), any(), any())(any(), any())
          }

          "and redirect to the corresponding URL when an active match is found" in {

            val previousSchemeNumber: String = allPreviousRegistrations.tail.head.previousSchemesDetails.value.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

            val activeMatch: Match = aMatch.copy(
              traderId = TraderId(traderId = previousSchemeNumber),
              memberState = previousRegistration2.previousEuCountry.code,
              exclusionStatusCode = None,
              exclusionEffectiveDate = None
            )

            val activeTrader = ActiveTraderResult(
              isReversal = false,
              exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
            )

            when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
            when(mockCoreRegistrationValidationService.searchScheme(
                eqTo(previousSchemeNumber),
                eqTo(allPreviousRegistrations.tail.head.previousSchemesDetails.value.tail.head.previousScheme.value),
                any(),
                eqTo(previousRegistration2.previousEuCountry.code))
              (any(), any())) thenReturn Some(activeMatch).toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

            val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
              .set(ActiveTraderResultQuery, activeTrader).success.value

            val result = service invokePrivate privateMethodCall(allPreviousRegistrations, hc, request)

            result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
            verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockCoreRegistrationValidationService, times(5)).searchScheme(any(), any(), any(), any())(any(), any())
          }

          "and redirect to the corresponding URL when a quarantined match is found" in {

            val previousSchemeNumber: String = allPreviousRegistrations.tail.head.previousSchemesDetails.value.tail.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

            val quarantinedMatch: Match = aMatch.copy(
              traderId = TraderId(traderId = previousSchemeNumber),
              memberState = previousRegistration2.previousEuCountry.code,
              exclusionStatusCode = Some(FailsToComply.numberValue),
              exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
            )

            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
            when(mockCoreRegistrationValidationService.searchScheme(
                eqTo(previousSchemeNumber),
                eqTo(allPreviousRegistrations.tail.head.previousSchemesDetails.value.tail.tail.head.previousScheme.value),
                any(),
                eqTo(previousRegistration2.previousEuCountry.code))
              (any(), any())) thenReturn Some(quarantinedMatch).toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

            val result = service invokePrivate privateMethodCall(allPreviousRegistrations, hc, request)

            result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
              exclusionExpiryDate = quarantinedMatch.getEffectiveDate
            ).url))
            verify(mockCoreRegistrationValidationService, times(6)).searchScheme(any(), any(), any(), any())(any(), any())
          }
        }

        "when it is an IOSS scheme" - {

          "and return None when an active match is found" in {

            val previousSchemeNumber: String = allPreviousRegistrations.head.previousSchemesDetails.value.head.previousSchemeNumbers.value.previousSchemeNumber.value

            val activeMatch: Match = aMatch.copy(
              traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
              memberState = allPreviousRegistrations.head.previousEuCountry.code,
              exclusionStatusCode = None,
              exclusionEffectiveDate = None
            )

            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

            val result = service invokePrivate privateMethodCall(allPreviousRegistrations, hc, request)

            result.futureValue `mustBe` None
            verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
              eqTo(previousSchemeNumber),
              eqTo(allPreviousRegistrations.head.previousSchemesDetails.value.head.previousScheme.value),
              eqTo(Some(intermediaryNumber)),
              eqTo(allPreviousRegistrations.head.previousEuCountry.code)
            )(any(), any())
          }

          "and redirect to the corresponding URL when a quarantined match is found" in {

            val previousSchemeNumber: String = allPreviousRegistrations.tail.head.previousSchemesDetails.value.head.previousSchemeNumbers.value.previousSchemeNumber.value

            val quarantinedMatch: Match = aMatch.copy(
              traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
              memberState = allPreviousRegistrations.tail.head.previousEuCountry.code,
              exclusionStatusCode = Some(ExclusionReason.FailsToComply.numberValue),
              exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(2).plusDays(1))
            )

            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn None.toFuture
            when(mockCoreRegistrationValidationService.searchScheme(
              eqTo(previousSchemeNumber),
              eqTo(allPreviousRegistrations.tail.head.previousSchemesDetails.value.head.previousScheme.value),
              eqTo(Some(intermediaryNumber)),
              eqTo(allPreviousRegistrations.tail.head.previousEuCountry.code)
            )(any(), any())) thenReturn Some(quarantinedMatch).toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

            val result = service invokePrivate privateMethodCall(Seq(previousRegistration1, previousRegistration2), hc, request)

            result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
              exclusionExpiryDate = quarantinedMatch.getEffectiveDate
            ).url))
            verify(mockCoreRegistrationValidationService, times(4)).searchScheme(any(), any(), any(), any())(any(), any())
          }
        }
      }
    }

    ".activeMatchRedirectUrl" - {

      "must return None when no active match is found" in {

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val activeMatchRedirectUrl = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

        val result = service invokePrivate activeMatchRedirectUrl(None, None, request)

        result.futureValue `mustBe` None
      }

      "when a match is found" - {

        "must set the active trader query path with active trader and redirect to the corresponding URL" in {

          val activeMatch: Match = arbitraryMatch.arbitrary.sample.value

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

          val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(ActiveTraderResultQuery, activeTrader).success.value

          val result = service invokePrivate privateMethodCall(Some(activeMatch), None, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        }

        "must redirect to corresponding URL when trader is quarantined" in {

          val quarantinedMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
          )

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

          val result = service invokePrivate privateMethodCall(Some(quarantinedMatch), None, request)

          result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
            exclusionExpiryDate = quarantinedMatch.getEffectiveDate
          )))
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        }
      }
    }

    ".revalidateUKVrn" - {

      "must return None when vrn is not expired and no match is found" in {

        val nonExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(nonExpiredVrnVatInfo))

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` None
      }

      "must redirect to the corresponding URL when the VRN is expired" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(expiredVrnVatInfo))

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val result = service invokePrivate privateMethodCall(hc, request)
        
        result.futureValue `mustBe` Some(Redirect(routes.RevalidateVrnExpiredController.onPageLoad().url))
      }

      "must redirect to the corresponding URL when the VRN is not expired and the VRN is already registered and active" in {

        val noneExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(noneExpiredVrnVatInfo))

        val activeMatch: Match = arbitraryMatch.arbitrary.sample.value

        val activeTrader = ActiveTraderResult(
          isReversal = false,
          exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
        )

        when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val expectedAnswers: UserAnswers = updatedUserAnswers
          .set(ActiveTraderResultQuery, activeTrader).success.value

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateAlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
      }

      "must redirect to the corresponding URL when the VRN is not expired and the VRN is already registered but quarantined" in {

        val noneExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(noneExpiredVrnVatInfo))

        val quarantinedMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
        )

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Some(quarantinedMatch).toFuture

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.RevalidateQuarantinedTraderController.onPageLoad(
          exclusionExpiryDate = quarantinedMatch.getEffectiveDate
        )))
        verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
        verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
      }
    }

    ".checkVrnExpired" - {

      "must return false if the VRN de-registration is not present" in {

        val nonExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = None
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate privateMethodCall(Some(nonExpiredVrnVatInfo))

        result `mustBe` false
      }

      "must return false if the VRN de-registration exists and the de-registration date is after today" in {

        val nonExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate privateMethodCall(Some(nonExpiredVrnVatInfo))

        result `mustBe` false
      }

      "must return true if the VRN de-registration exists and the de-registration date is today" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate privateMethodCall(Some(expiredVrnVatInfo))

        result `mustBe` true
      }

      "must return true if the VRN de-registration exists and the de-registration date is before today" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate privateMethodCall(Some(expiredVrnVatInfo))

        result `mustBe` true
      }
    }
  }
}
