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

package connectors.returns

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.*
import models.domain.returns.VatReturn
import models.responses.*
import org.scalacheck.Arbitrary.arbitrary
import org.scalatest.EitherValues
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier

import java.time.Instant
import java.util.UUID

class VatReturnConnectorSpec extends SpecBase with WireMockHelper with EitherValues {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()
  val url = "/one-stop-shop-returns/vat-returns"

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-returns.port" -> server.port)
      .build()

  ".get" - {

    val vatReturn = arbitrary[VatReturn].sample.value
    val responseJson = Json.toJson(vatReturn)

    "must return Right(VatReturn) when the server responds with OK" in {

      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(OK).withBody(responseJson.toString())
            ))

        connector.get(period).futureValue mustBe Right(vatReturn)
      }
    }

    "must return Left(NotFound) when the server responds with NOT_FOUND" in {

      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(NOT_FOUND)
            ))

        connector.get(period).futureValue mustBe Left(NotFound)
      }
    }

    "must return Left(InvalidJson) when the server responds with invalid JSON" in {
      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        val invalidJson = """{ "unexpectedField": "value" }"""

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(OK).withBody(invalidJson)
            ))

        connector.get(period).futureValue mustBe Left(InvalidJson)
      }
    }

    "must return Left(RegistrationNotFound) when the server responds with NOT_FOUND and 'RegistrationNotFound' in body" in {
      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(NOT_FOUND).withBody(CoreErrorResponse.REGISTRATION_NOT_FOUND)
            ))

        connector.get(period).futureValue mustBe Left(RegistrationNotFound)
      }
    }

    "must return Left(ReceivedErrorFromCore) when the server responds with SERVICE_UNAVAILABLE and valid error JSON" in {
      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        val timestamp = Instant.now()
        val transactionId = UUID.randomUUID()
        val errorCode = "OSS_009"
        val errorMessage = "Service temporarily unavailable"


        val errorJson =
          s"""{
                "timestamp": "$timestamp",
                "transactionId": "$transactionId",
                "errorCode": "$errorCode",
                "errorMessage": "$errorMessage"
          }"""

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(SERVICE_UNAVAILABLE).withBody(errorJson)
            ))

        connector.get(period).futureValue mustBe Left(ReceivedErrorFromCore)
      }
    }

    "must return Left(ConflictFound) when the server responds with CONFLICT" in {
      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(CONFLICT)
            ))

        connector.get(period).futureValue mustBe Left(ConflictFound)
      }
    }

    "must return Left(UnexpectedResponseStatus) when the server responds with an unexpected status" in {
      running(application) {
        val connector = application.injector.instanceOf[VatReturnConnector]

        server.stubFor(
          get(urlEqualTo(s"$url/period/${period.toString}"))
            .willReturn(
              aResponse().withStatus(418)
            ))

        connector.get(period).futureValue mustBe Left(UnexpectedResponseStatus(418, "Unexpected response, status 418 returned"))
      }
    }
  }

}
