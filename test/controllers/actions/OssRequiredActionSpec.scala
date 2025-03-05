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

package controllers.actions

import base.SpecBase
import controllers.routes
import models.domain.Registration
import models.requests.{AuthenticatedDataRequest, AuthenticatedMandatoryDataRequest}
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import testutils.RegistrationData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class OssRequiredActionSpec extends SpecBase {

  private val registration: Registration = RegistrationData.registration

  class Harness extends OssRequiredActionImpl {

    def callRefine[A](request: AuthenticatedDataRequest[A]): Future[Either[Result, AuthenticatedMandatoryDataRequest[A]]] = {
      refine(request)
    }
  }

  "Oss Required Action" - {

    "must return Left and Redirect to Not Registered when registration doesn't exist in the request" in {

      val action = new Harness

      val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, None, emptyUserAnswers)

      val result = action.callRefine(request).futureValue

      result mustBe Left(Redirect(routes.NotRegisteredController.onPageLoad().url))
    }

    "must return Right Authenticated Mandatory Data Request when a registration exists in the request" in {

      val action = new Harness

      val request = AuthenticatedDataRequest(FakeRequest(), testCredentials, vrn, Some(registration), emptyUserAnswers)

      val result = action.callRefine(request).futureValue

      val expectedResult = AuthenticatedMandatoryDataRequest(request, testCredentials, vrn, registration, emptyUserAnswers)

      result mustBe Right(expectedResult)
    }
  }
}
