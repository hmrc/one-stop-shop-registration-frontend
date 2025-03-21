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
import config.FrontendAppConfig
import controllers.routes
import models.AmendMode
import models.requests.AuthenticatedDataRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckNiProtocolFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(appConfig: FrontendAppConfig) extends CheckNiProtocolFilterImpl(None, appConfig) {
    def callFilter(request: AuthenticatedDataRequest[_]): Future[Option[Result]] = filter(request)
  }

  ".filter" - {

    "when Ni protocol validation toggle is true" - {

      "must return None when singleMarketIndicator is true" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must redirect to Ni Protocol Rejection page when singleMarketIndicator is false" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithSingleMarketIndicatorFalse = basicUserAnswersWithVatInfo copy (vatInfo =
          Some(vatCustomerInfo copy (singleMarketIndicator = Some(false))))

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, userAnswersWithSingleMarketIndicatorFalse, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result.value mustEqual Redirect(routes.NiProtocolRejectionController.onPageLoad())
        }
      }

      "must redirect to Start Amend Journey page when singleMarketIndicator is false and is in Amend Mode" in {

        val app = applicationBuilder(None, mode = Some(AmendMode))
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithSingleMarketIndicatorFalse = basicUserAnswersWithVatInfo copy (vatInfo =
          Some(vatCustomerInfo copy (singleMarketIndicator = Some(false))))

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, userAnswersWithSingleMarketIndicatorFalse, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result.value mustEqual Redirect(routes.NiProtocolRejectionController.onPageLoad())
        }
      }

      "must throw an Exception when SingleMarketIndicator is None" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithSingleMarketIndicatorNone = basicUserAnswersWithVatInfo copy (vatInfo =
          Some(vatCustomerInfo copy (singleMarketIndicator = None)))

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, userAnswersWithSingleMarketIndicatorNone, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = intercept[Exception](controller.callFilter(request).futureValue)

          result.getMessage must include("Illegal State Exception")
        }
      }

      "must return None when VatCustomerInfo is not present" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithVatCustomerInfoNone = basicUserAnswersWithVatInfo copy (vatInfo = None)

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, userAnswersWithVatCustomerInfoNone, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

    }

    "when Ni protocol validation toggle is false" - {

      "must return None" in {
        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> false
          ).build()

        running(app) {
          val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, basicUserAnswersWithVatInfo, None, 0, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }
    }
  }
}

