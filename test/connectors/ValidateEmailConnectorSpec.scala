/*
 * Copyright 2022 HM Revenue & Customs
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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, post, urlEqualTo}
import models.{ValidateEmailRequest, VerifyEmail}
import play.api.Application
import play.api.http.Status.{BAD_REQUEST, CREATED}
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class ValidateEmailConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private val verifyEmail: VerifyEmail =
    VerifyEmail(
      address = "test@test.com",
      enterUrl = "testUrl"
    )

  private val validateEmailRequest: ValidateEmailRequest =
    ValidateEmailRequest(
      credId = userAnswersId,
      continueUrl = "/testUrl",
      origin = "oss",
      deskproServiceName = None,
      accessibilityStatementUrl = "test",
      pageTitle = None,
      backUrl = None,
      email = Some(verifyEmail)
    )

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.email-verification.port" -> server.port)
      .build()

  ".verifyEmail" - {

    "must return CREATED when verify email request initiated" in {

      running(application) {

        val connector = application.injector.instanceOf[ValidateEmailConnector]

        server.stubFor(
          post(urlEqualTo("/hmrc/verify-email"))
            .willReturn(aResponse.withStatus(201))
        )

        connector.verifyEmail(validateEmailRequest).futureValue mustBe CREATED
      }

    }

    "must return false when email not verified" in {

      running(application) {

        val connector = application.injector.instanceOf[ValidateEmailConnector]

        server.stubFor(
          post(urlEqualTo("/hmrc/verify-email"))
            .willReturn(aResponse.withStatus(400))
        )

        connector.verifyEmail(validateEmailRequest).futureValue mustBe BAD_REQUEST
      }
    }

  }

}
