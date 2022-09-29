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
import models.EmailVerificationResponse
import models.responses.UnexpectedResponseStatus
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status._
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class EmailVerificationConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  val url = "/email-verification/verify-email"

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.email-verification.port" -> server.port)
      .build()

  ".verifyEmail" - {

    "must return Right(CREATED) when verify email request initiated" in {

      running(application) {

        val connector = application.injector.instanceOf[EmailVerificationConnector]

        val expectedEmailVerificationResponse = EmailVerificationResponse(redirectUri = "/test")

        val responseBody = Json.toJson(expectedEmailVerificationResponse).toString

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse
              .withStatus(CREATED)
              .withBody(responseBody)
            )
        )

        connector.verifyEmail(emailVerificationRequest).futureValue mustBe Right(expectedEmailVerificationResponse)
      }

    }

    "must return Left(UnexpectedResponseStatus) when the server responds with an Http Exception" in {

      running(application) {

        val errorCode = Gen.oneOf(BAD_REQUEST, UNAUTHORIZED, INTERNAL_SERVER_ERROR, BAD_GATEWAY).sample.value

        val connector = application.injector.instanceOf[EmailVerificationConnector]

        server.stubFor(
          post(urlEqualTo(url))
            .willReturn(aResponse.withStatus(errorCode))
        )

        connector.verifyEmail(emailVerificationRequest)
          .futureValue mustBe Left(
          UnexpectedResponseStatus(errorCode, s"Unexpected response, status $errorCode returned")
        )
      }
    }

  }

}
