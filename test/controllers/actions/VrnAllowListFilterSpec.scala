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
import models.requests.AuthenticatedIdentifierRequest
import org.scalatest.BeforeAndAfterEach
import org.scalatestplus.mockito.MockitoSugar
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import services.FeatureFlagService
import uk.gov.hmrc.auth.core.Enrolments

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class VrnAllowListFilterSpec extends SpecBase with MockitoSugar with BeforeAndAfterEach {

  class Harness(features: FeatureFlagService) extends VrnAllowListFilterImpl(features) {
    def callFilter(request: AuthenticatedIdentifierRequest[_]): Future[Option[Result]] = filter(request)
  }

  ".filter" - {

    "when restricting access using the VRN allow list is enabled" - {

      "and the user's VRN is in the allow list" - {

        "must return None" in {

          val app =
            applicationBuilder(None)
              .configure(
                "restrict-access-using-vrn-allow-list" -> true,
                "vrn-allow-list" -> Seq(vrn.value),
                "vrn-blocked-redirect-url" -> "foo"
              )
              .build()

          running(app) {
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), None)
            val features = app.injector.instanceOf[FeatureFlagService]
            val controller = new Harness(features)

            val result = controller.callFilter(request).futureValue

            result must not be defined
          }
        }
      }

      "and the user's VRN is not in the allow list" - {

        "must Redirect to the redirect URL" in {

          val app =
            applicationBuilder(None)
              .configure(
                "features.restrict-access-using-vrn-allow-list" -> true,
                "features.vrn-allow-list" -> Seq.empty,
                "features.vrn-blocked-redirect-url" -> "foo"
              )
              .build()

          running(app) {
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), None)
            val features = app.injector.instanceOf[FeatureFlagService]
            val controller = new Harness(features)

            val result = controller.callFilter(request).futureValue

            result.value mustEqual Redirect("foo")
          }
        }
      }
    }

    "when restricting access using the VRN allow list is disabled" - {

      "and the user's VRN is not in the allow list" - {

        "must return None" in {

          val app =
            applicationBuilder(None)
              .configure(
                "restrict-access-using-vrn-allow-list" -> false,
                "vrn-allow-list" -> Seq.empty,
                "vrn-blocked-redirect-url" -> "foo"
              )
              .build()

          running(app) {
            val request = AuthenticatedIdentifierRequest(FakeRequest(), testCredentials, vrn, Enrolments(Set.empty), None)
            val features = app.injector.instanceOf[FeatureFlagService]
            val controller = new Harness(features)

            val result = controller.callFilter(request).futureValue

            result must not be defined
          }
        }
      }
    }
  }
}
