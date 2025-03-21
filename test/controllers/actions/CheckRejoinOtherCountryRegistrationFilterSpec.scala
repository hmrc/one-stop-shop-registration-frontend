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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import models.RejoinMode
import models.core.{Match, MatchType}
import models.requests.AuthenticatedDataRequest
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.EitherValues
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import services.CoreRegistrationValidationService
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckRejoinOtherCountryRegistrationFilterSpec extends SpecBase with MockitoSugar with EitherValues {

  private val mockCoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  class Harness(frontendAppConfig: FrontendAppConfig)
    extends CheckRejoinOtherCountryRegistrationFilterImpl(Some(RejoinMode), mockCoreRegistrationValidationService, frontendAppConfig) {
    def callFilter(request: AuthenticatedDataRequest[_]): Future[Option[Result]] = filter(request)
  }

  private lazy val globalController = (frontendAppConfig: FrontendAppConfig) =>
    new Harness(frontendAppConfig)

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "DE",
    None,
    None,
    exclusionEffectiveDate = Some(LocalDate.now),
    None,
    None
  )

  ".filter" - {

    "when other country registration validation is enabled" - {

      "must redirect to RejoinAlreadyRegisteredOtherCountry page when matchType=OtherMSNETPActiveNETP" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.OtherMSNETPActiveNETP)).toFuture

          val result = controller.callFilter(request).futureValue.value
          result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
        }
      }

      "must redirect to RejoinAlreadyRegisteredOtherCountry page when matchType=FixedEstablishmentActiveNETP" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.FixedEstablishmentActiveNETP)).toFuture

          val result = controller.callFilter(request).futureValue.value
          result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
        }
      }

      "must redirect to CannotRejoinQuarantinedCountryController page when matchType=OtherMSNETPQuarantinedNETP" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.OtherMSNETPQuarantinedNETP)).toFuture

          val result = controller.callFilter(request).futureValue.value
          result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
        }
      }

      "must redirect to CannotRejoinQuarantinedCountryController page when matchType=FixedEstablishmentQuarantinedNETP" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.FixedEstablishmentQuarantinedNETP)).toFuture

          val result = controller.callFilter(request).futureValue.value
          result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
        }
      }

      "must redirect to CannotRejoinQuarantinedCountryController page when exclusion status code is 4" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.TransferringMSID, exclusionStatusCode = Some(4))).toFuture

          val result = controller.callFilter(request).futureValue.value
          result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
            genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
        }
      }

      "must return None when matchType=TransferringMSID" in {

        val app = applicationBuilder(None)
          .overrides(bind[CoreRegistrationValidationService].toInstance(mockCoreRegistrationValidationService))
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          when(mockCoreRegistrationValidationService.searchUkVrn(any())(any(), any())) thenReturn
            Some(genericMatch.copy(matchType = MatchType.TransferringMSID)).toFuture

          val result = controller.callFilter(request).futureValue
          result mustBe None
        }
      }
    }

    "when other country registration validation is disabled" - {

      "must return None" in {

        val app = applicationBuilder(None)
          .configure("other-country-reg-validation-enabled" -> "false")
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val frontendAppConfig = app.injector.instanceOf[FrontendAppConfig]
          val controller: Harness = globalController(frontendAppConfig)

          val result = controller.callFilter(request).futureValue
          result mustBe None
        }
      }
    }
  }
}

