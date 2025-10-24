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

package services

import base.SpecBase
import models.core.{Match, TraderId}
import models.domain.*
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import models.{Country, PreviousScheme}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, when}
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import testutils.RegistrationData
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class RejoinPreviousRegistrationValidationServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val rejoinPreviousRegistrationValidationService =
    new RejoinPreviousRegistrationValidationService(coreRegistrationValidationService, stubClockAtArbitraryDate)

  private val registration: Registration = RegistrationData.registration

  private implicit val hc: HeaderCarrier = HeaderCarrier()

  private implicit val request: AuthenticatedMandatoryDataRequest[_] = AuthenticatedMandatoryDataRequest(
    AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, Some(registration), emptyUserAnswers, None, 0, None),
    testCredentials,
    vrn,
    registration,
    emptyUserAnswers
  )

  private val genericMatch = Match(
    TraderId("33333333"),
    None,
    "DE",
    None,
    None,
    exclusionEffectiveDate = Some(LocalDate.now),
    None,
    None
  )

  private val previousRegistrationNew: PreviousRegistrationNew =
    PreviousRegistrationNew(
      Country("AT", "Austria"),
      Seq(PreviousSchemeDetails(PreviousScheme.OSSU, PreviousSchemeNumbers("123", None), nonCompliantDetails = None))
    )

  override def beforeEach(): Unit = {
    reset(coreRegistrationValidationService)
  }

  ".validatePreviousRegistrations" - {

    "must redirect to CannotRejoinQuarantinedCountryController when the previous registration matches to a quarantined trader" in {

      val quarantinedTraderMatch: Match = genericMatch.copy(exclusionStatusCode = Some(4))

      when(coreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())).thenReturn(Some(quarantinedTraderMatch).toFuture)

      val result = rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(Seq(previousRegistrationNew)).futureValue.value
      result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
        genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
    }

    "must redirect to RejoinAlreadyRegisteredOtherCountryController when the previous registration matches to an active trader" - {

      val activeTraderMatch: Match = genericMatch

      when(coreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())).thenReturn(Some(activeTraderMatch).toFuture)

      val result = rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(Seq(previousRegistrationNew)).futureValue.value
      result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
    }

    "must not redirect when the previous registration is neither a quarantined trader nor an active trader" - {

      when(coreRegistrationValidationService.searchScheme(any(), any(), any(), any())(any(), any())).thenReturn(None.toFuture)

      val result = rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(Seq(previousRegistrationNew)).futureValue
      result mustBe None
    }

    "must not redirect when there is no previous registrations" - {

      val result = rejoinPreviousRegistrationValidationService.validatePreviousRegistrations(Seq.empty).futureValue
      result mustBe None
    }
  }
}
