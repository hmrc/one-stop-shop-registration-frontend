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
import models.core.{Match, MatchType}
import models.domain.*
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import models.{Country, InternationalAddress}
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

class RejoinEuRegistrationValidationServiceSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  private val coreRegistrationValidationService: CoreRegistrationValidationService = mock[CoreRegistrationValidationService]

  private val rejoinEuRegistrationValidationService = new RejoinEuRegistrationValidationService(coreRegistrationValidationService)

  private val registration: Registration = RegistrationData.registration

  implicit private val hc: HeaderCarrier = HeaderCarrier()
  implicit private val request: AuthenticatedMandatoryDataRequest[_] = AuthenticatedMandatoryDataRequest(
    AuthenticatedDataRequest(FakeRequest("GET", "/"), testCredentials, vrn, Some(registration), emptyUserAnswers, None, 0, None),
    testCredentials,
    vrn,
    registration,
    emptyUserAnswers
  )

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

  val euVatRegistration: EuVatRegistration = EuVatRegistration(Country("AT", "Austria"), "123456789")

  val registrationWithFixedEstablishment: RegistrationWithFixedEstablishment = RegistrationWithFixedEstablishment(
    Country("AT", "Austria"),
    EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
    TradeDetails(
      "Irish trading name",
      InternationalAddress(
        line1 = "Line 1",
        line2 = None,
        townOrCity = "Town",
        stateOrRegion = None,
        None,
        Country("IE", "Ireland")
      ))
  )

  val registrationWithoutFixedEstablishmentWithTradeDetails: RegistrationWithoutFixedEstablishmentWithTradeDetails =
    RegistrationWithoutFixedEstablishmentWithTradeDetails(
      Country("AT", "Austria"),
      EuTaxIdentifier(EuTaxIdentifierType.Vat, Some("123456789")),
      TradeDetails(
        "Irish trading name",
        InternationalAddress(
          line1 = "Line 1",
          line2 = None,
          townOrCity = "Town",
          stateOrRegion = None,
          None,
          Country("IE", "Ireland")
        ))
    )

  override def beforeEach(): Unit = {
    reset(coreRegistrationValidationService)
  }

  ".validateEuRegistrations" - {

    "must redirect to CannotRejoinQuarantinedCountryController" - {

      val quarantinedTraderMatch: Match = genericMatch.copy(matchType = MatchType.TraderIdQuarantinedNETP)

      "when the EU VAT registration matches to a quarantined trader" in {

        when(coreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())).thenReturn(Some(quarantinedTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(euVatRegistration)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
          genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
      }

      "when the registration with fixed establishment matches to a quarantined trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(Some(quarantinedTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithFixedEstablishment)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
          genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
      }

      "when the registration without fixed establishment matches to a quarantined trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(Some(quarantinedTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithoutFixedEstablishmentWithTradeDetails)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.CannotRejoinQuarantinedCountryController.onPageLoad(
          genericMatch.memberState, genericMatch.exclusionEffectiveDate.mkString))
      }
    }

    "must redirect to RejoinAlreadyRegisteredOtherCountryController" - {

      val activeTraderMatch = genericMatch.copy(matchType = MatchType.TraderIdActiveNETP)

      "when the EU VAT registration matches to an active trader" in {

        val euVatRegistration = EuVatRegistration(Country("AT", "Austria"), "123456789")

        when(coreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())).thenReturn(Some(activeTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(euVatRegistration)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
      }

      "when the registration with fixed establishment matches to an active trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(Some(activeTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithFixedEstablishment)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
      }

      "when the registration without fixed establishment matches to an active trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(Some(activeTraderMatch).toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithoutFixedEstablishmentWithTradeDetails)).futureValue.value
        result mustBe Redirect(controllers.rejoin.routes.RejoinAlreadyRegisteredOtherCountryController.onPageLoad(genericMatch.memberState))
      }
    }

    "must not redirect" - {

      "when the EU VAT registration matches to no trader" in {

        when(coreRegistrationValidationService.searchEuVrn(any(), any(), any())(any(), any())).thenReturn(None.toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(euVatRegistration)).futureValue
        result mustBe None
      }

      "when the registration with fixed establishment matches to no trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(None.toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithFixedEstablishment)).futureValue
        result mustBe None
      }

      "when the registration without fixed establishment matches to no trader" in {

        when(coreRegistrationValidationService.searchEuTaxId(any(), any())(any(), any())).thenReturn(None.toFuture)

        val result = rejoinEuRegistrationValidationService.validateEuRegistrations(Seq(registrationWithoutFixedEstablishmentWithTradeDetails)).futureValue
        result mustBe None
      }
    }
  }
}