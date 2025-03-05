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
import models.RejoinMode
import models.requests.AuthenticatedOptionalDataRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.NiProtocolExpiredService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckNiProtocolExpiredOptionalFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(appConfig: FrontendAppConfig) extends CheckNiProtocolExpiredOptionalFilterImpl(new NiProtocolExpiredService(appConfig), Some(RejoinMode)) {
    def callFilter(request: AuthenticatedOptionalDataRequest[_]): Future[Option[Result]] = filter(request)
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
          val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(basicUserAnswersWithVatInfo))
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must redirect to Ni Protocol Expired page when singleMarketIndicator is false" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithSingleMarketIndicatorFalse = basicUserAnswersWithVatInfo.copy(vatInfo =
          Some(vatCustomerInfo.copy(singleMarketIndicator = Some(false))))

        running(app) {
          val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(userAnswersWithSingleMarketIndicatorFalse))
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result.value mustEqual Redirect(routes.NiProtocolExpiredController.onPageLoad())
        }
      }

      "must return None when UserAnswers is not present" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        running(app) {
          val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, None)
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }

      "must return None when VatCustomerInfo is not present" in {

        val app = applicationBuilder(None)
          .configure(
            "features.reg-validation-enabled" -> true
          )
          .build()

        val userAnswersWithVatCustomerInfoNone = basicUserAnswersWithVatInfo.copy(vatInfo = None)

        running(app) {
          val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(userAnswersWithVatCustomerInfoNone))
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
          val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(basicUserAnswersWithVatInfo))
          val config = app.injector.instanceOf[FrontendAppConfig]
          val controller = new Harness(config)

          val result = controller.callFilter(request).futureValue

          result must not be defined
        }
      }
    }

  }
}
