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

package controllers.rejoin

import base.SpecBase
import org.mockito.ArgumentMatchers.{any, eq => eqTo}
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.AuthenticatedUserAnswersRepository
import testutils.WireMockHelper
import uk.gov.hmrc.http.SessionKeys
import views.html.rejoin.RejoinJourneyRecoveryView

import scala.concurrent.Future

class RejoinJourneyRecoveryControllerSpec extends SpecBase with WireMockHelper {

  private val redirectUrl = "http://localhost:10204/pay-vat-on-goods-sold-to-eu/northern-ireland-returns-payments/"

  "RejoinJourneyRecovery Controller" - {

      "must delete all user answers and return OK and the correct view for a GET" in {

        val mockSessionRepository = mock[AuthenticatedUserAnswersRepository]

        when(mockSessionRepository.clear(any())) thenReturn Future.successful(true)

        val application =
          applicationBuilder(userAnswers = Some(basicUserAnswersWithVatInfo))
            .overrides(bind[AuthenticatedUserAnswersRepository].toInstance(mockSessionRepository))
            .build()

        running(application) {
          val sessionId = userAnswersId
          val request = FakeRequest(GET, controllers.rejoin.routes.RejoinJourneyRecoveryController.onPageLoad().url)
            .withSession(SessionKeys.sessionId -> sessionId)

          val result = route(application, request).value

          val view = application.injector.instanceOf[RejoinJourneyRecoveryView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(redirectUrl)(request, messages(application)).toString
          verify(mockSessionRepository, times(1)).clear(eqTo(sessionId))
        }
      }

  }
}
