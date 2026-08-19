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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.RejoinMode
import models.requests.AuthenticatedDataRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import java.time.LocalDate
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckVatExpiredFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness() extends CheckVatExpiredFilterImpl(Some(RejoinMode), stubClockAtArbitraryDate) {
    def callFilter(request: AuthenticatedDataRequest[_]): Future[Option[Result]] = filter(request)
  }

  ".filter" - {

    "must return None when no dereg date exists" in {

      val app = applicationBuilder(None)
        .build()

      val vatInfo = vatCustomerInfo.copy(deregistrationDecisionDate = None)

      running(app) {
        val request = AuthenticatedDataRequest(
          FakeRequest(),
          testCredentials,
          vrn,
          None,
          basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatInfo)),
          None,
          0,
          None
        )
        val controller = new Harness()

        val result = controller.callFilter(request).futureValue

        result must not be defined
      }
    }

    "must return None when dereg is in the future" in {

      val app = applicationBuilder(None)
        .build()


      val vatInfo = vatCustomerInfo.copy(deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).plusYears(1)))

      running(app) {
        val request = AuthenticatedDataRequest(
          FakeRequest(),
          testCredentials,
          vrn,
          None,
          basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatInfo)),
          None,
          0,
          None
        )
        val controller = new Harness()

        val result = controller.callFilter(request).futureValue

        result must not be defined
      }
    }


    "must redirect to invalid vrn date page when dereg date is in the past" in {

      val app = applicationBuilder(None)
        .build()


      val vatInfo = vatCustomerInfo.copy(deregistrationDecisionDate = Some(LocalDate.now(stubClockAtArbitraryDate).minusYears(1)))

      running(app) {
        val request = AuthenticatedDataRequest(
          FakeRequest(),
          testCredentials,
          vrn,
          None,
          basicUserAnswersWithVatInfo.copy(vatInfo = Some(vatInfo)),
          None,
          0,
          None
        )
        val controller = new Harness()

        val result = controller.callFilter(request).futureValue

        result.value mustEqual Redirect(routes.InvalidVrnDateController.onPageLoad())
      }
    }
  }
}

