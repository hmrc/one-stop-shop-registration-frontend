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

package connectors.test

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, get, urlEqualTo}
import org.scalacheck.Gen
import play.api.Application
import play.api.http.Status.{NOT_FOUND, OK, UNAUTHORIZED}
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier


class TestOnlyEmailPasscodeConnectorSpec extends SpecBase with WireMockHelper {

  private implicit val hc = HeaderCarrier()

  val url = "/test-only/passcodes"

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.email-verification.port" -> server.port)
      .build()

  ".getTestOnlyPasscode" - {

    "must return passcode when connector returns a response" in {

      running(application) {

        val connector = application.injector.instanceOf[TestOnlyEmailPasscodeConnector]

        val response = """{"passcodes":[{"email": "ppt@mail.com", "passcode": "HDDDYX"}]}"""

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse
              .withStatus(OK)
              .withBody(response)
            )
        )

        val expectedResponse = EmailPasscodes(Seq(EmailPasscodeEntry("ppt@mail.com", "HDDDYX")))

        connector.getTestOnlyPasscode().futureValue mustBe Right(expectedResponse)

      }

    }

    "must return UnexpectedResponseStatus when connector returns an error" in {

      running(application) {

        val errorCode = Gen.oneOf(NOT_FOUND, UNAUTHORIZED).sample.value

        val connector = application.injector.instanceOf[TestOnlyEmailPasscodeConnector]

        val expectedResponse = DownstreamServiceError(
          s"Received unexpected response code $errorCode",
          FailedToFetchTestOnlyPasscode("Failed to get test only passcodes")
        )

        server.stubFor(
          get(urlEqualTo(url))
            .willReturn(aResponse
              .withStatus(errorCode)
            )
        )

        connector.getTestOnlyPasscode().futureValue mustBe Left(expectedResponse)

      }

    }

  }

}
