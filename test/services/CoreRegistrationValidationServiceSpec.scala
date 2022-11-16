/*
 * Copyright 2022 HM Revenue & Customs
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
import connectors.ValidateCoreRegistrationConnector
import models.core.{CoreRegistrationValidationResult, Match, MatchType}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CoreRegistrationValidationServiceSpec extends SpecBase {

  private val genericMatch = Match(
    MatchType.FixedEstablishmentActiveNETP,
    "333333333",
    None,
    "EE",
    Some(2),
    None,
    None,
    None,
    None
  )

  private val coreValidationResponses: CoreRegistrationValidationResult =
    CoreRegistrationValidationResult(
      "333333333",
      None,
      "EE",
      traderFound = true,
      Seq(
        genericMatch
      ))

  private val connector = mock[ValidateCoreRegistrationConnector]

  implicit val hc: HeaderCarrier = HeaderCarrier()

  "coreRegistrationValidationService.searchUkVrn" - {

    "call searchUkVrn for any matchType and return match data" in {

      val vrn = Vrn("333333333")

      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Right(coreValidationResponses))

      val coreRegistrationValidationService = new CoreRegistrationValidationService(connector)

      val value = coreRegistrationValidationService.searchUkVrn(vrn).futureValue.get

      value equals genericMatch
    }

    "must return None when no active match found" in {

      val vrn = Vrn("333333333")

      val expectedResponse = coreValidationResponses.copy(matches = Seq[Match]())
      when(connector.validateCoreRegistration(any())) thenReturn Future.successful(Right(expectedResponse))

      val coreRegistrationValidationService = new CoreRegistrationValidationService(connector)

      val value = coreRegistrationValidationService.searchUkVrn(vrn).futureValue

      value mustBe None
    }
  }
}
