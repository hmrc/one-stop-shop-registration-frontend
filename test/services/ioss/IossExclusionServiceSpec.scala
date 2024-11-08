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

package services.ioss

import base.SpecBase
import connectors.RegistrationConnector
import models.iossExclusions.EtmpExclusionReason.FailsToComply
import models.iossExclusions.{EtmpDisplayRegistration, EtmpExclusion, EtmpExclusionReason}
import models.responses.RegistrationNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.MockitoSugar.when
import org.scalacheck.Gen
import org.scalatest.PrivateMethodTester
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global

class IossExclusionServiceSpec extends SpecBase with PrivateMethodTester {

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val currentDate: LocalDate = LocalDate.now(stubClockAtArbitraryDate)

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private val iossEtmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

  "IossExclusionService" - {

    ".isQuarantinedCode4" - {

      "must return true when an IOSS excluded trader is both quarantined and the exclusion reason is code 4 and the effective date is within 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = EtmpExclusionReason.FailsToComply,
          effectiveDate = currentDate.minusYears(2),
          quarantine = true
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe true
      }

      "must return false when an IOSS excluded trader is both quarantined and the exclusion reason is code 4 but the effective date is outside 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = EtmpExclusionReason.FailsToComply,
          effectiveDate = currentDate.minusYears(2).minusDays(1),
          quarantine = true
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe false
      }

      "must return false when an IOSS excluded trader is not quarantined and the exclusion reason is code 4 but the effective date is outside 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = EtmpExclusionReason.FailsToComply,
          effectiveDate = currentDate.minusYears(2).minusDays(1),
          quarantine = false
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe false
      }

      "must return false when an IOSS excluded trader is not quarantined but the exclusion reason is not code 4 but the effective date is outside 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = Gen.oneOf(EtmpExclusionReason.values).retryUntil(x => x != FailsToComply).sample.value,
          effectiveDate = currentDate.minusYears(2).minusDays(1),
          quarantine = false
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe false
      }

      "must return false when an IOSS excluded trader is not quarantined but the exclusion reason is not code 4 but the effective date is inside 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = Gen.oneOf(EtmpExclusionReason.values).retryUntil(x => x != FailsToComply).sample.value,
          effectiveDate = currentDate.minusYears(2),
          quarantine = false
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe false
      }

      "must return false when an IOSS excluded trader is not quarantined but the exclusion reason is code 4 but the effective date is outside 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          exclusionReason = EtmpExclusionReason.FailsToComply,
          effectiveDate = currentDate.minusYears(2).minusDays(1),
          quarantine = false
        )

        val updatedIossEtmpDisplayRegistration: EtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(exclusions = Seq(updatedIossEtmpExclusion))

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.isQuarantinedCode4().futureValue

        result mustBe false
      }
    }

    ".getIossEtmpExclusion" - {

      "must return an IossEtmpExclusion when IOSS backend returns a successful valid payload" in {

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(iossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.getIossEtmpExclusion().futureValue

        result mustBe iossEtmpDisplayRegistration.exclusions.headOption
      }

      "must return None when IOSS backend returns an Etmp Display Registration with Exclusions as an empty Sequence" in {

        val updatedIossEtmpDisplayRegistration = iossEtmpDisplayRegistration.copy(
          exclusions = Seq.empty
        )

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Right(updatedIossEtmpDisplayRegistration).toFuture

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.getIossEtmpExclusion().futureValue

        result mustBe None
      }

      "must throw an Exception when a IOSS ETMP Display Registration can't be retrieved" in {

        when(mockRegistrationConnector.getIossRegistration()(any())) thenReturn Left(RegistrationNotFound).toFuture

        val exceptionMessage: String = s"An error occurred whilst retrieving the IOSS ETMP Display Registration with error: $RegistrationNotFound"

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val result = service.getIossEtmpExclusion()

        whenReady(result.failed) { exp =>
          exp mustBe a[Exception]
          exp.getMessage mustBe exceptionMessage
        }
      }
    }

    ".isAfterTwoYears" - {

      "must return true when the effective date is after 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          effectiveDate = currentDate.minusYears(2).minusDays(1)
        )

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val isAfterTwoYearsMethod = PrivateMethod[Boolean](Symbol("isAfterTwoYears"))

        val result = service invokePrivate isAfterTwoYearsMethod(updatedIossEtmpExclusion)

        result mustBe true
      }

      "must return false when the effective date is before 2 years" in {

        val updatedIossEtmpExclusion: EtmpExclusion = iossEtmpDisplayRegistration.exclusions.head.copy(
          effectiveDate = currentDate.minusYears(2)
        )

        val service = new IossExclusionService(stubClockAtArbitraryDate, mockRegistrationConnector)

        val isAfterTwoYearsMethod = PrivateMethod[Boolean](Symbol("isAfterTwoYears"))

        val result = service invokePrivate isAfterTwoYearsMethod(updatedIossEtmpExclusion)

        result mustBe false
      }
    }
  }
}
