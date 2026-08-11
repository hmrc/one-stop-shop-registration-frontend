/*
 * Copyright 2025 HM Revenue & Customs
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
import config.Constants.{intermediaryEnrolmentKey, iossEnrolmentKey}
import connectors.RegistrationConnector
import models.enrolments.{EACDEnrolment, EACDEnrolments}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global

class AccountServiceSpec extends SpecBase {

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val eACDEnrolment1: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    identifiers = Seq(arbitraryEACDIdentifiers.arbitrary.sample.value.copy(key = iossEnrolmentKey))
  )

  private val eACDEnrolment2: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    identifiers = Seq(arbitraryEACDIdentifiers.arbitrary.sample.value.copy(key = iossEnrolmentKey))
  )

  private val eACDIntermediaryEnrolment1: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    identifiers = Seq(arbitraryEACDIdentifiers.arbitrary.sample.value.copy(key = intermediaryEnrolmentKey))
  )

  private val eACDIntermediaryEnrolment2: EACDEnrolment = arbitraryEACDEnrolment.arbitrary.sample.value.copy(
    identifiers = Seq(arbitraryEACDIdentifiers.arbitrary.sample.value.copy(key = intermediaryEnrolmentKey))
  )

  private val eACDEnrolments: EACDEnrolments = arbitraryEACDEnrolments.arbitrary.sample.value
    .copy(enrolments = Seq(eACDEnrolment1, eACDEnrolment2))

  private val eACDIntermediaryEnrolments: EACDEnrolments = arbitraryEACDEnrolments.arbitrary.sample.value
    .copy(enrolments = Seq(eACDIntermediaryEnrolment1, eACDIntermediaryEnrolment2))

  "AccountService" - {

    "must retrieve the latest IOSS account if one exists" in {

      when(mockRegistrationConnector.getAccounts()(any())) thenReturn eACDEnrolments.toFuture

      val service = new AccountService(mockRegistrationConnector)

      val result = service.getLatestAccount().futureValue

      result mustBe Some(eACDEnrolments.enrolments.maxBy(_.activationDate).identifiers.head.value)
    }

    "must return None when no IOSS accounts are retrieved" in {

      when(mockRegistrationConnector.getAccounts()(any())) thenReturn arbitraryEACDEnrolments.arbitrary.sample.value.toFuture

      val service = new AccountService(mockRegistrationConnector)

      val result = service.getLatestAccount().futureValue

      result mustBe None
    }
  }

  "getLatestIntermediaryAccount" - {

    "must retrieve the latest IOSS account if one exists" in {

      when(mockRegistrationConnector.getIntermediaryAccounts()(any())) thenReturn eACDIntermediaryEnrolments.toFuture

      val service = new AccountService(mockRegistrationConnector)

      val result = service.getLatestIntermediaryAccount().futureValue

      result mustBe Some(eACDIntermediaryEnrolments.enrolments.maxBy(_.activationDate).identifiers.head.value)
    }

    "must return None when no IOSS accounts are retrieved" in {

      when(mockRegistrationConnector.getIntermediaryAccounts()(any())) thenReturn arbitraryEACDEnrolments.arbitrary.sample.value.toFuture

      val service = new AccountService(mockRegistrationConnector)

      val result = service.getLatestIntermediaryAccount().futureValue

      result mustBe None
    }
  }
}
