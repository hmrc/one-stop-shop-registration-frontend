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
import config.FrontendAppConfig
import models.{CheckMode, RejoinLoopMode, RejoinMode}
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock



class NiProtocolExpiredServiceSpec extends SpecBase {

  private val config = mock[FrontendAppConfig]
  private val niProtocolExpiredService = new NiProtocolExpiredService(config)

  "isNiProtocolExpired" - {
    "return true when registration validation is enabled and singleMarketIndicator is false and mode is RejoinMode " in {
      when(config.registrationValidationEnabled) thenReturn true
      niProtocolExpiredService.isNiProtocolExpired(Some(RejoinMode), singleMarketIndicator = Some(false)) mustBe true
    }

    "return true when registration validation is enabled and singleMarketIndicator is false and mode is RejoinLoopMode" in {
      when(config.registrationValidationEnabled) thenReturn true
      niProtocolExpiredService.isNiProtocolExpired(Some(RejoinLoopMode), singleMarketIndicator = Some(false)) mustBe true
    }

    "return false when registration validation is disabled" in {
      when(config.registrationValidationEnabled) thenReturn false
      niProtocolExpiredService.isNiProtocolExpired(Some(RejoinMode), singleMarketIndicator = Some(false)) mustBe false
    }

    "return false when mode is neither RejoinMode nor RejoinLoopMode" in {
      when(config.registrationValidationEnabled) thenReturn true
      niProtocolExpiredService.isNiProtocolExpired(Some(CheckMode), singleMarketIndicator = Some(false)) mustBe false
    }

    "return false when singleMarketIndicator is true" in {
      when(config.registrationValidationEnabled) thenReturn true
      niProtocolExpiredService.isNiProtocolExpired(Some(RejoinMode), singleMarketIndicator = Some(true)) mustBe false
    }
  }
}
