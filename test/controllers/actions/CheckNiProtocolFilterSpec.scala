/*
 * Copyright 2023 HM Revenue & Customs
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
import connectors.RegistrationConnector
import controllers.routes
import models.RegistrationValidationResult
import models.requests.AuthenticatedIdentifierRequest
import models.responses.UnexpectedResponseStatus
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject.bind
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckNiProtocolFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {


/* TODO VEOSS-1054
class Harness(appConfig: FrontendAppConfig) extends CheckNiProtocolFilterImpl(appConfig) {
  def callFilter(request: AuthenticatedIdentifierRequest[_]): Future[Option[Result]] = filter(request)
}

".filter" - {
  "when Ni protocol validation toggle is true" - {
    "must return None when RegistrationValidationResult is true" in {

      when(mockConnector.validateRegistration(any())(any())) thenReturn Future.successful(Right(RegistrationValidationResult(true)))

      val app = applicationBuilder(None)
        .configure(
          "features.reg-validation-enabled" -> true
        )
        .overrides(
          bind[RegistrationConnector].toInstance(mockConnector)
        ).build()

      running(app) {
        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val config = app.injector.instanceOf[FrontendAppConfig]
        val controller = new Harness(mockConnector, config)

        val result = controller.callFilter(request).futureValue

        result must not be defined
      }
    }

    "must redirect to Ni Protocol Rejection page when RegistrationValidationResult is false" in {

      when(mockConnector.validateRegistration(any())(any())) thenReturn Future.successful(Right(RegistrationValidationResult(false)))

      val app = applicationBuilder(None)
        .configure(
          "features.reg-validation-enabled" -> true
        )
        .overrides(
          bind[RegistrationConnector].toInstance(mockConnector)
        ).build()


      running(app) {
        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val config = app.injector.instanceOf[FrontendAppConfig]
        val controller = new Harness(mockConnector, config)

        val result = controller.callFilter(request).futureValue

        result.value mustEqual Redirect(routes.NiProtocolRejectionController.onPageLoad())
      }
    }

    "must throw an Exception when registration connector returns an error" in {

      when(mockConnector.validateRegistration(any())(any())) thenReturn Future.successful(Left(UnexpectedResponseStatus(123, "unknown error")))

      val app = applicationBuilder(None)
        .configure(
          "features.reg-validation-enabled" -> true
        )
        .overrides(
          bind[RegistrationConnector].toInstance(mockConnector)
        ).build()


      running(app) {
        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val config = app.injector.instanceOf[FrontendAppConfig]
        val controller = new Harness(mockConnector, config)

        val result = intercept[Exception](controller.callFilter(request).futureValue)

        result.getMessage must include("unknown error")
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
        val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty))
        val config = app.injector.instanceOf[FrontendAppConfig]
        val controller = new Harness(mockConnector, config)

        val result = controller.callFilter(request).futureValue

        result must not be defined
      }
    }


  }
}*/
}
