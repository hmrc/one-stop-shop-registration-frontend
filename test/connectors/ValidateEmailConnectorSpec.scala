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
import models.responses.UnexpectedResponseStatus
import models.{ValidateEmailRequest, ValidateEmailResponse, VerifyEmail}
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.{BAD_GATEWAY, BAD_REQUEST, CREATED, INTERNAL_SERVER_ERROR, UNAUTHORIZED}
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class ValidateEmailConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val url = "/email-verification/verify-email"

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

  ".validateEmail" - {

    "must return CREATED when verify email request initiated" in {

      running(application) {

        val connector = application.injector.instanceOf[ValidateEmailConnector]

        val expectedValidateEmailResponse = ValidateEmailResponse(redirectUrl = "/test")

        val responseBody = Json.toJson(expectedValidateEmailResponse).toString

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse
              .withStatus(CREATED)
              .withBody(responseBody)
            )
        )

        connector.validateEmail(validateEmailRequest).futureValue mustBe Right(expectedValidateEmailResponse)
      }

    }

    "must return Left(UnexpectedResponseStatus) when the server responds with an Http Exception" in {

      running(application) {

        val errorCode = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

        val connector = application.injector.instanceOf[ValidateEmailConnector]

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(errorCode))
        )

        connector.validateEmail(validateEmailRequest)
          .futureValue mustBe Left(
          UnexpectedResponseStatus(errorCode, s"Unexpected response, status $errorCode returned")
        )
      }
    }

  }

}
