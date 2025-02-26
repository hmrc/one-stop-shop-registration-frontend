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
import controllers.amend.routes as amendRoutes
import controllers.routes
import models.domain.Registration
import models.requests.{AuthenticatedDataRequest, AuthenticatedOptionalDataRequest, UnauthenticatedDataRequest, UnauthenticatedOptionalDataRequest}
import models.{AmendMode, Mode, NormalMode}
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.{GET, running}
import testutils.RegistrationData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuthenticatedDataRequiredActionSpec extends SpecBase with MockitoSugar {

  private val registration: Registration = RegistrationData.registration

  class Harness(mode: Mode) extends AuthenticatedDataRequiredActionImpl(Some(mode)) {

    def callRefine(request: AuthenticatedOptionalDataRequest[_]): Future[Either[Result, AuthenticatedDataRequest[_]]] = refine(request)
  }

  class UnauthenticatedHarness() extends UnauthenticatedDataRequiredAction {

    def callRefine(request: UnauthenticatedOptionalDataRequest[_]): Future[Either[Result, UnauthenticatedDataRequest[_]]] = refine(request)

  }

  "AuthenticatedDataRequiredActionImpl" - {

    ".refine" - {

      "in Normal Mode" - {

        "must redirect to Journey Recovery when there no userAnswers present" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, None)
            val action = new Harness(NormalMode)

            val result = action.callRefine(request).futureValue

            result `mustBe` Left(Redirect(routes.JourneyRecoveryController.onPageLoad().url))
          }
        }

        "must redirect to Journey Recovery On Missing Answers when there are empty userAnswers present" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(emptyUserAnswers))
            val action = new Harness(NormalMode)

            val result = action.callRefine(request).futureValue

            result `mustBe` Left(Redirect(routes.JourneyRecoveryController.onMissingAnswers().url))
          }
        }

        "must return Right(AuthenticatedDataRequest) with no Registration when there is data present" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = FakeRequest(GET, "test/url")
            val action = new Harness(NormalMode)

            val result = action.callRefine(AuthenticatedOptionalDataRequest(request, testCredentials, vrn, None, Some(basicUserAnswersWithVatInfo))).futureValue

            result `mustBe` Right(AuthenticatedDataRequest(request, testCredentials, vrn, None, basicUserAnswersWithVatInfo))
          }
        }
      }

      "in Amend Mode" - {

        "must return Right(AuthenticatedDataRequest) with a Registration when there is data present" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = FakeRequest(GET, "test/url")
            val action = new Harness(AmendMode)

            val result = action.callRefine(AuthenticatedOptionalDataRequest(request, testCredentials, vrn, Some(registration), Some(basicUserAnswersWithVatInfo))).futureValue

            result `mustBe` Right(AuthenticatedDataRequest(request, testCredentials, vrn, Some(registration), basicUserAnswersWithVatInfo))
          }
        }

        "must redirect to Amend Journey Recovery when there is data present but no registration has been retrieved" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(basicUserAnswersWithVatInfo))
            val action = new Harness(AmendMode)

            val result = action.callRefine(request).futureValue

            result `mustBe` Left(Redirect(amendRoutes.AmendJourneyRecoveryController.onPageLoad().url))
          }
        }

        "must redirect to Amend Journey Recovery when there are empty userAnswers present" in {

          val application = applicationBuilder().build()

          running(application) {

            val request = AuthenticatedOptionalDataRequest(FakeRequest(), testCredentials, vrn, None, Some(emptyUserAnswers))
            val action = new Harness(AmendMode)

            val result = action.callRefine(request).futureValue

            result `mustBe` Left(Redirect(amendRoutes.AmendJourneyRecoveryController.onPageLoad().url))
          }
        }
      }
    }
  }

  "UnauthenticatedDataRequiredAction" - {

    ".refine" - {

      "must redirect to Registered For Oss In Eu Controller when there no userAnswers present" in {

        val application = applicationBuilder().build()

        running(application) {

          val request = FakeRequest(GET, "test/url")
          val action = new UnauthenticatedHarness

          val result = action.callRefine(UnauthenticatedOptionalDataRequest(request, testCredentials.providerId, None)).futureValue

          result `mustBe` Left(Redirect(routes.RegisteredForOssInEuController.onPageLoad().url))
        }
      }

      "must return Right(UnauthenticatedDataRequest) when there are userAnswers present" in {

        val application = applicationBuilder().build()

        running(application) {

          val request = FakeRequest(GET, "test/url")
          val action = new UnauthenticatedHarness

          val result = action.callRefine(UnauthenticatedOptionalDataRequest(request, testCredentials.providerId, Some(basicUserAnswersWithVatInfo))).futureValue

          result `mustBe` Right(UnauthenticatedDataRequest(request, testCredentials.providerId, basicUserAnswersWithVatInfo))
        }
      }
    }
  }
}

