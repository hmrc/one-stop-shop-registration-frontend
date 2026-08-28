package services

import base.SpecBase
import controllers.routes
import models.PreviousScheme.{IOSSWI, OSSNU, OSSU}
import models.core.{Match, TraderId}
import models.domain.VatCustomerInfo
import models.exclusions.ExclusionReason
import models.exclusions.ExclusionReason.FailsToComply
import models.previousRegistrations.{PreviousRegistrationDetailsWithOptionalVatNumber, SchemeDetailsWithOptionalVatNumber}
import models.requests.AuthenticatedDataRequest
import models.{ActiveTraderResult, Country, UserAnswers}
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.{BeforeAndAfterEach, PrivateMethodTester}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import queries.ActiveTraderResultQuery
import repositories.AuthenticatedUserAnswersRepository
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

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockCoreRegistrationValidationService,
      mockAuthenticatedUserAnswersRepository
    )
  }

  "SavedAnswersRevalidationService" - {

    // TODO
    ".revalidateSavedUserAnswers" - {

      "when validating UK VRN" - {

        "must return None when UK VRN is not already registered, expired or quarantined" in {

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn None.toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          implicit val request: AuthenticatedDataRequest[AnyContent] = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val result = service.revalidateSavedUserAnswers().futureValue

          result `mustBe` None
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
        }

        "must redirect to Already Registered when UK VRN is already registered" in {

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

          result `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
        }
      }

      // TODO -> Previous Reg checks
      // TODO -> EuDetails checks
    }

    // TODO
    ".revalidatePreviousSchemeDetails" - {

      "must return true ....." in {

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val revalidatePreviousSchemeDetails = PrivateMethod[Boolean](Symbol("revalidatePreviousSchemeDetails"))

        val result = service invokePrivate revalidatePreviousSchemeDetails()

        result `mustBe` true
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

        "must redirect to the corresponding URL when an active match is found" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.tail.tail.head.previousSchemeNumbers.value.previousSchemeNumber.value

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
            eqTo(allPreviousSchemeDetails.tail.tail.head.previousScheme.value),
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

          result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(3)).searchScheme(
            any(),
            any(),
            any(),
            eqTo(countryCode)
          )(any(), any())
        }

        "must return the corresponding URL when a quarantined match is found" in {

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

          result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(
            countryCode = quarantinedMatch.memberState,
            exclusionDate = quarantinedMatch.getEffectiveDate
          ).url))
          verify(mockCoreRegistrationValidationService, times(2)).searchScheme(any(), any(), any(), eqTo(countryCode))(any(), any())
        }
      }

      "when it is an IOSS scheme" - {

        "must return the corresponding URL when an active match is found" in {

          val previousSchemeNumber: String = allPreviousSchemeDetails.head.previousSchemeNumbers.value.previousSchemeNumber.value

          val activeMatch: Match = aMatch.copy(
            traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
            memberState = countryCode,
            exclusionStatusCode = None,
            exclusionEffectiveDate = None
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
          when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidatePreviousSchemeDetails"))

          // TODO -> Match Some(intermediaryNumber) in scheme
          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails)

          val activeTrader = ActiveTraderResult(
            isReversal = activeMatch.exclusionStatusCode.contains(-1),
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          val expectedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(ActiveTraderResultQuery, activeTrader).success.value

          result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad().url))
          verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            eqTo(previousSchemeNumber),
            eqTo(allPreviousSchemeDetails.head.previousScheme.value),
            eqTo(Some(intermediaryNumber)),
            eqTo(countryCode)
          )(any(), any())
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedUserAnswers))
        }

        "must return the corresponding URL when a quarantined match is found" in {

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

          // TODO -> Match Some(intermediaryNumber) in scheme
          val result = service invokePrivate privateMethodCall(countryCode, allPreviousSchemeDetails)

          result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(
            countryCode = quarantinedMatch.memberState,
            exclusionDate = quarantinedMatch.getEffectiveDate
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

    // TODO -> Fix these tests
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

          // TODO -> Fix, scheme called 5 times instead of 6 so check how many schemes
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

            result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
            verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
            verify(mockCoreRegistrationValidationService, times(6)).searchScheme(any(), any(), any(), any())(any(), any())
          }

          "and return the corresponding URL when a quarantined match is found" in {

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

            result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(
              countryCode = quarantinedMatch.memberState,
              exclusionDate = quarantinedMatch.getEffectiveDate
            ).url))
            verify(mockCoreRegistrationValidationService, times(6)).searchScheme(any(), any(), any(), any())(any(), any())
          }
        }

        // TODO
        "when it is an IOSS scheme" - {

          "and return the corresponding URL when an active match is found" in {

            val previousSchemeNumber: String = allPreviousRegistrations.head.previousSchemesDetails.value.head.previousSchemeNumbers.value.previousSchemeNumber.value

            val activeMatch: Match = aMatch.copy(
              traderId = TraderId(traderId = s"IM$previousSchemeNumber"),
              memberState = allPreviousRegistrations.head.previousEuCountry.code,
              exclusionStatusCode = None,
              exclusionEffectiveDate = None
            )

            when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture
            when(mockCoreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())) thenReturn Some(activeMatch).toFuture

            val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

            val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateAllPreviousRegistrations"))

            val result = service invokePrivate privateMethodCall(allPreviousRegistrations, Some(intermediaryNumber))

            val activeTrader = ActiveTraderResult(
              isReversal = activeMatch.exclusionStatusCode.contains(-1),
              exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
            )

            val expectedUserAnswers: UserAnswers = emptyUserAnswersWithVatInfo
              .set(ActiveTraderResultQuery, activeTrader).success.value

            result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad().url))
            // TODO
            //            verify(mockCoreRegistrationValidationService, times(1)).searchScheme(
            //              eqTo(previousSchemeNumber),
            //              eqTo(allPreviousRegistrations.head.previousSchemesDetails.value.head.previousScheme.value),
            //              eqTo(Some(intermediaryNumber)),
            //              eqTo(allPreviousRegistrations.head.previousEuCountry.code)
            //            )(any(), any())
            //            verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedUserAnswers))
          }

          "and return the corresponding URL when a quarantined match is found" in {

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

            val result = service invokePrivate privateMethodCall(Seq(previousRegistration1, previousRegistration2), Some(intermediaryNumber))

            result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(
              countryCode = quarantinedMatch.memberState,
              exclusionDate = quarantinedMatch.getEffectiveDate
            ).url))
            // TODO
            //            verify(mockCoreRegistrationValidationService, times(4)).searchScheme(any(), any(), any(), any())(any(), any())
          }
        }
      }
    }

    ".activeMatchRedirectUrl" - {

      "must return None when no active match is found" in {

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val activeMatchRedirectUrl = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

        val result = service invokePrivate activeMatchRedirectUrl(None, request)

        result.futureValue `mustBe` None
      }

      "when an active match is found" - {

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

          val result = service invokePrivate privateMethodCall(Some(activeMatch), request)

          result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        }

        "must redirect to corresponding URL when trader is quarantined" in {

          val activeMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
          )

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

          val result = service invokePrivate privateMethodCall(Some(activeMatch), request)

          result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate)))
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

        // TODO -> Change redirect when new one created
        result.futureValue `mustBe` Some(Redirect(routes.JourneyRecoveryController.onPageLoad().url))
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

        result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad().url))
        verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
      }

      "must redirect to the corresponding URL when the VRN is not expired and the VRN is already registered but quarantined" in {

        val noneExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(noneExpiredVrnVatInfo))

        val activeMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
        )

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val privateMethodCall = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val result = service invokePrivate privateMethodCall(hc, request)

        result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate).url))
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
