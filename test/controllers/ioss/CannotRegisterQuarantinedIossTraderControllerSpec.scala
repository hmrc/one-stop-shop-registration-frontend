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

package controllers.ioss

import base.SpecBase
import formats.Format.quarantinedIOSSRegistrationFormatter
import models.iossRegistration.{IossEtmpExclusion, IossEtmpExclusionReason}
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.ioss.IossExclusionService
import utils.FutureSyntax.FutureOps
import views.html.ioss.CannotRegisterQuarantinedIossTraderView

import java.time.LocalDate

class CannotRegisterQuarantinedIossTraderControllerSpec extends SpecBase {

  private val mockIossExclusionService: IossExclusionService = mock[IossExclusionService]

  private val iossEtmpExclusion: IossEtmpExclusion = {
    IossEtmpExclusion(
      exclusionReason = IossEtmpExclusionReason.FailsToComply,
      effectiveDate = LocalDate.now(stubClockAtArbitraryDate),
      decisionDate = LocalDate.now(stubClockAtArbitraryDate),
      quarantine = true
    )
  }

  "CannotRegisterQuarantinedIossTrader Controller" - {

    "must return OK and the correct view for a GET" in {

      when(mockIossExclusionService.getIossEtmpExclusion(any())(any())) thenReturn Some(iossEtmpExclusion).toFuture

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo), iossNumber = Some(iossNumber))
        .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotRegisterQuarantinedIossTraderController.onPageLoad().url)

        val result = route(application, request).value

        val view = application.injector.instanceOf[CannotRegisterQuarantinedIossTraderView]

        val formattedExcludeEndDate: String = iossEtmpExclusion.effectiveDate.plusYears(2).plusDays(1).format(quarantinedIOSSRegistrationFormatter)

        status(result) `mustBe` OK
        contentAsString(result) `mustBe` view(formattedExcludeEndDate)(request, messages(application)).toString
      }
    }

    "must throw an IllegalStateException for a GET when the expected ETMP Exclusion is missing" in {

      val exceptionMessage: String = "Expected an ETMP Exclusion"

      when(mockIossExclusionService.getIossEtmpExclusion(any())(any())) thenReturn None.toFuture

      val application = applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo), iossNumber = Some(iossNumber))
        .overrides(bind[IossExclusionService].toInstance(mockIossExclusionService))
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotRegisterQuarantinedIossTraderController.onPageLoad().url)

        val result = route(application, request).value

        whenReady(result.failed) { exp =>
          exp mustBe a[IllegalStateException]
          exp.getMessage mustBe exceptionMessage
        }
      }
    }

    "must throw an IllegalStateException for a GET when the expected IOSS number is missing" in {

      val exceptionMessage: String = "Expected an IOSS number"

      val application = applicationBuilder(userAnswers = None)
        .build()

      running(application) {
        val request = FakeRequest(GET, routes.CannotRegisterQuarantinedIossTraderController.onPageLoad().url)

        val result = route(application, request).value

        whenReady(result.failed) { exp =>
          exp mustBe a[IllegalStateException]
          exp.getMessage mustBe exceptionMessage
        }
      }
    }
  }
}
