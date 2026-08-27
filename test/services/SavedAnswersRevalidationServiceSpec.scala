package services

import base.SpecBase
import controllers.routes
import models.{ActiveTraderResult, UserAnswers}
import models.core.Match
import models.domain.VatCustomerInfo
import models.exclusions.ExclusionReason.FailsToComply
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.{any, eq as eqTo}
import org.mockito.Mockito
import org.mockito.Mockito.{times, verify, verifyNoInteractions, when}
import org.scalatest.{BeforeAndAfterEach, PrivateMethodTester}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import queries.ActiveTraderResultQuery
import repositories.AuthenticatedUserAnswersRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SavedAnswersRevalidationServiceSpec extends SpecBase with PrivateMethodTester with BeforeAndAfterEach {

  private val mockCoreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]
  private val mockAuthenticatedUserAnswersRepository: AuthenticatedUserAnswersRepository = mock[AuthenticatedUserAnswersRepository]

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  override def beforeEach(): Unit = {
    Mockito.reset(
      mockCoreRegistrationValidationService,
      mockAuthenticatedUserAnswersRepository
    )
  }

  "SavedAnswersRevalidationService" - {

    // TODO
    ".revalidateSavedUserAnswers" - {

      "must return true" in {

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val result = service.revalidateSavedUserAnswers()

        result `mustBe` true
      }
    }

    // TODO - need app builder?
    ".activeMatchRedirectUrl" - {

      "must return None when no active match is found" in {

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val activeMatchRedirectUrl = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

        val result = service invokePrivate activeMatchRedirectUrl(None, request)

        result.futureValue `mustBe` None
      }

      "when an active match is found" - {

        "must set the active trader query path with active trader and redirect to Already Registered" in {

          val activeMatch: Match = arbitraryMatch.arbitrary.sample.value

          val activeTrader = ActiveTraderResult(
            isReversal = false,
            exclusionEffectiveDate = activeMatch.exclusionEffectiveDate
          )

          when(mockAuthenticatedUserAnswersRepository.set(any())) thenReturn true.toFuture

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val activeMatchRedirectUrl = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

          val expectedAnswers: UserAnswers = emptyUserAnswersWithVatInfo
            .set(ActiveTraderResultQuery, activeTrader).success.value

          val result = service invokePrivate activeMatchRedirectUrl(Some(activeMatch), request)

          result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad()))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
        }

        "must redirect to Other Country Excluded And Quarantined page when trader is quarantined" in {

          val activeMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
            exclusionStatusCode = Some(FailsToComply.numberValue),
            exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
          )

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, emptyUserAnswersWithVatInfo, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val activeMatchRedirectUrl = PrivateMethod[Future[Option[Result]]](Symbol("activeMatchRedirectUrl"))

          val result = service invokePrivate activeMatchRedirectUrl(Some(activeMatch), request)

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

        val application = applicationBuilder()
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(application) {

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val revalidateUKVrn = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

          val result = service invokePrivate revalidateUKVrn(hc, request)

          result.futureValue `mustBe` None
        }
      }

      // TODO -> Add new redirect page to test title
      "must redirect to ???? when the VRN is expired" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(expiredVrnVatInfo))

        val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val revalidateUKVrn = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

        val result = service invokePrivate revalidateUKVrn(hc, request)

        // TODO -> Change redirect when new one created
        result.futureValue `mustBe` Some(Redirect(routes.JourneyRecoveryController.onPageLoad().url))
      }

      "must redirect to Already Registered when the VRN is not expired and the VRN is already registered and active" in {

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

        val application = applicationBuilder()
          .overrides(
            bind[AuthenticatedUserAnswersRepository].toInstance(mockAuthenticatedUserAnswersRepository),
            bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService)
          )
          .build()

        running(application) {

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val revalidateUKVrn = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

          val expectedAnswers: UserAnswers = updatedUserAnswers
            .set(ActiveTraderResultQuery, activeTrader).success.value

          val result = service invokePrivate revalidateUKVrn(hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.AlreadyRegisteredController.onPageLoad().url))
          verify(mockAuthenticatedUserAnswersRepository, times(1)).set(eqTo(expectedAnswers))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
        }
      }

      "must redirect to Other Country Excluded And Quarantined page when the VRN is not expired and the VRN is already registered but quarantined" in {

        val noneExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val updatedUserAnswers = emptyUserAnswersWithVatInfo.copy(vatInfo = Some(noneExpiredVrnVatInfo))

        val activeMatch: Match = arbitraryMatch.arbitrary.sample.value.copy(
          exclusionStatusCode = Some(FailsToComply.numberValue),
          exclusionEffectiveDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
        )

        when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn Some(activeMatch).toFuture

        val application = applicationBuilder()
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(application) {

          val request = AuthenticatedDataRequest[AnyContent](FakeRequest(), testCredentials, vrn, None, updatedUserAnswers, None, 0, None)

          val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

          val revalidateUKVrn = PrivateMethod[Future[Option[Result]]](Symbol("revalidateUKVrn"))

          val result = service invokePrivate revalidateUKVrn(hc, request)

          result.futureValue `mustBe` Some(Redirect(routes.OtherCountryExcludedAndQuarantinedController.onPageLoad(activeMatch.memberState, activeMatch.getEffectiveDate).url))
          verify(mockCoreRegistrationValidationService, times(1)).searchUkVrn(eqTo(vrn))(any(), any())
          verifyNoInteractions(mockAuthenticatedUserAnswersRepository)
        }
      }
    }

    ".checkVrnExpired" - {

      "must return false if the VRN de-registration is not present" in {

        val nonExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = None
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val checkVrnExpired = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate checkVrnExpired(Some(nonExpiredVrnVatInfo))

        result `mustBe` false
      }

      "must return false if the VRN de-registration exists and the de-registration date is after today" in {

        val nonExpiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusDays(1))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val checkVrnExpired = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate checkVrnExpired(Some(nonExpiredVrnVatInfo))

        result `mustBe` false
      }

      "must return true if the VRN de-registration exists and the de-registration date is today" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val checkVrnExpired = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate checkVrnExpired(Some(expiredVrnVatInfo))

        result `mustBe` true
      }

      "must return true if the VRN de-registration exists and the de-registration date is before today" in {

        val expiredVrnVatInfo: VatCustomerInfo = vatCustomerInfo.copy(
          deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusDays(1))
        )

        val service = new SavedAnswersRevalidationService(mockCoreRegistrationValidationService, mockAuthenticatedUserAnswersRepository, stubClockAtArbitraryDate)

        val checkVrnExpired = PrivateMethod[Boolean](Symbol("checkVrnExpired"))

        val result = service invokePrivate checkVrnExpired(Some(expiredVrnVatInfo))

        result `mustBe` true
      }
    }
  }
}
