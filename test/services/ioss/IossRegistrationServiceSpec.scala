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
import connectors.RegistrationConnector
import models.iossRegistration.IossEtmpDisplayRegistration
import models.responses.RegistrationNotFound
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import uk.gov.hmrc.http.HeaderCarrier
import utils.FutureSyntax.FutureOps

import scala.concurrent.ExecutionContext.Implicits.global

class IossRegistrationServiceSpec extends SpecBase {

  private val mockRegistrationConnector: RegistrationConnector = mock[RegistrationConnector]

  private implicit val hc: HeaderCarrier = new HeaderCarrier()

  private val iossEtmpDisplayRegistration: IossEtmpDisplayRegistration = arbitraryIossEtmpDisplayRegistration.arbitrary.sample.value

  "IossRegistrationService" - {

    "must return Right(IossEtmpDisplayRegistration) when connector returns a valid payload" in {

      when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Right(iossEtmpDisplayRegistration).toFuture

      val service = new IossRegistrationService(mockRegistrationConnector)

      val result = service.getIossRegistration(Some(iossNumber)).futureValue

      result mustBe Some(iossEtmpDisplayRegistration)
    }

    "must return None when connector returns Left(Error)" in {

      when(mockRegistrationConnector.getIossRegistration(any())(any())) thenReturn Left(RegistrationNotFound).toFuture

      val service = new IossRegistrationService(mockRegistrationConnector)

      val result = service.getIossRegistration(Some(iossNumber)).futureValue

      result mustBe None
    }
  }
}
