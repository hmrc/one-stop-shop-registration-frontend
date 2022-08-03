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
import com.github.tomakehurst.wiremock.client.WireMock._
import models.RegistrationValidationResult
import models.responses.{ConflictFound, InvalidJson, NotFound, UnexpectedResponseStatus}
import org.scalacheck.Gen
import play.api.Application
import play.api.libs.json.Json
import play.api.test.Helpers._
import testutils.{RegistrationData, WireMockHelper}
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationRequestConnectorSpec extends SpecBase with WireMockHelper {

  private val registration = RegistrationData.registration

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def getValidateRegistrationUrl(vrn: Vrn) = s"/one-stop-shop-registration/registration/validate/${vrn.value}"

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-registration.port" -> server.port)
      .build()

  "submitRegistration" - {

    "must return Right when a new registration is created on the backend" in {

      val url = s"/one-stop-shop-registration/create"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(created()))

        val result = connector.submitRegistration(registration).futureValue

        result mustEqual Right(())
      }
    }

    "must return Left(ConflictFound) when the backend returns CONFLICT" in {

      val url = s"/one-stop-shop-registration/create"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(CONFLICT)))

        val result = connector.submitRegistration(registration).futureValue

        result mustEqual Left(ConflictFound)
      }
    }

    "must return Left(ConflictFound) when the backend returns UnexpectedResponseStatus" in {

      val url = s"/one-stop-shop-registration/create"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(123)))

        val result = connector.submitRegistration(registration).futureValue

        result mustEqual Left(UnexpectedResponseStatus(123, "Unexpected response, status 123 returned"))
      }
    }
  }

  "getRegistration" - {

    "must return a registration when the backend returns one" in {

      val url = s"/one-stop-shop-registration/registration"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody = Json.toJson(RegistrationData.registration).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getRegistration().futureValue

        result.value mustEqual RegistrationData.registration
      }
    }

    "must return None when the backend returns NOT_FOUND" in {

      val url = s"/one-stop-shop-registration/registration"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.getRegistration().futureValue

        result must not be defined
      }
    }
  }

  "getVatCustomerInfo" - {

    val url = s"/one-stop-shop-registration/vat-information"

    "must return information when the backend returns some" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val vatInfo = vatCustomerInfo

        val responseBody = Json.toJson(vatInfo).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getVatCustomerInfo().futureValue

        result mustEqual Right(vatInfo)
      }
    }

    "must return invalid json when the backend returns some" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody = Json.obj("test" -> "test").toString()

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getVatCustomerInfo().futureValue

        result mustEqual Left(InvalidJson)
      }
    }

    "must return Left(NotFound) when the backend returns NOT_FOUND" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.getVatCustomerInfo().futureValue

        result mustEqual Left(NotFound)
      }
    }

    "must return Left(UnexpectedStatus) when the backend returns another error code" in {

      val status = Gen.oneOf(400, 500, 501, 502, 503).sample.value

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)))

        val result = connector.getVatCustomerInfo().futureValue

        result mustEqual Left(UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
      }
    }
  }

  ".validateRegistration" - {

    "must return a Right(ValidateRegistration) when the server returns OK for a recognised payload" in {

      val vrn = Vrn("111111111")

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val validateRegistration = RegistrationValidationResult(
          validRegistration = true
        )

        val responseBody = Json.toJson(validateRegistration).toString()

        server.stubFor(get(urlEqualTo(getValidateRegistrationUrl(vrn))).willReturn(ok().withBody(responseBody)))

        val result = connector.validateRegistration(vrn).futureValue

        result mustBe Right(validateRegistration)
      }

    }

    "must return Left(NotFound) when the server returns NOT_FOUND" in {

      val vrn = Vrn("111111111")

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(getValidateRegistrationUrl(vrn))).willReturn(notFound()))

        val result = connector.validateRegistration(vrn).futureValue

        result mustBe Left(NotFound)
      }
    }

    "must return Left(UnexpectedStatus) when the backend returns another error code" in {

      val vrn = Vrn("111111111")
      val status = Gen.oneOf(400, 500, 501, 502, 503).sample.value

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(getValidateRegistrationUrl(vrn))).willReturn(aResponse().withStatus(status)))

        val result = connector.validateRegistration(vrn).futureValue

        result mustBe Left(UnexpectedResponseStatus(status, s"Unexpected response, status $status returned"))
      }
    }
  }

}