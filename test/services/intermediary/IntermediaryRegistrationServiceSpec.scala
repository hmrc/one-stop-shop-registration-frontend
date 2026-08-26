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

package services.intermediary

import base.SpecBase
import connectors.RegistrationConnector
import models.etmp.intermediary.IntermediaryRegistrationWrapper
import models.iossRegistration.IossEtmpDisplayRegistration
import models.responses.RegistrationNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global

class IntermediaryRegistrationServiceSpec extends SpecBase {

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val intermediaryEtmpDisplayRegistration: IntermediaryRegistrationWrapper = registrationWrapper

  "IntermediaryRegistrationService" - {

    "must return Right(IossEtmpDisplayRegistration) when connector returns a valid payload" in {

      when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Right(intermediaryEtmpDisplayRegistration).toFuture

      val service = new IntermediaryRegistrationService(mockRegistrationConnector)

      val result = service.getIntermediaryRegistration(Some(intNumber)).futureValue

      result mustBe Some(intermediaryEtmpDisplayRegistration)
    }

    "must return None when connector returns Left(Error)" in {

      when(mockRegistrationConnector.getIntermediaryRegistration(any())(any())) thenReturn Left(RegistrationNotFound).toFuture

      val service = new IntermediaryRegistrationService(mockRegistrationConnector)

      val result = service.getIntermediaryRegistration(Some(intNumber)).futureValue

      result mustBe None
    }
  }
}
