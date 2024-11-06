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

package connectors

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.external.ExternalEntryUrl
import models.iossExclusions.EtmpDisplayRegistration
import models.responses.{ConflictFound, InvalidJson, NotFound, UnexpectedResponseStatus}
import org.scalacheck.Gen
import play.api.Application
import play.api.libs.json.Json
import play.api.test.Helpers._
import testutils.{RegistrationData, WireMockHelper}
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationRequestConnectorSpec extends SpecBase with WireMockHelper {

  private val registration = RegistrationData.registration

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-registration.port" -> server.port)
      .configure("microservice.services.ioss-registration.port" -> server.port)
      .build()

  "submitRegistration" - {

    val url = s"/one-stop-shop-registration/create"

    "must return Right when a new registration is created on the backend" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(created()))

        val result = connector.submitRegistration(registration).futureValue

        result mustBe Right(())
      }
    }

    "must return Left(ConflictFound) when the backend returns CONFLICT" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(CONFLICT)))

        val result = connector.submitRegistration(registration).futureValue

        result mustBe Left(ConflictFound)
      }
    }

    "must return Left(UnexpectedResponseStatus) when the backend returns UnexpectedResponseStatus" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(123)))

        val result = connector.submitRegistration(registration).futureValue

        result mustBe Left(UnexpectedResponseStatus(123, "Unexpected response, status 123 returned"))
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

        result.value mustBe RegistrationData.registration
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

        result mustBe Right(vatInfo)
      }
    }

    "must return invalid json when the backend returns some" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody = Json.obj("test" -> "test").toString()

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getVatCustomerInfo().futureValue

        result mustBe Left(InvalidJson)
      }
    }

    "must return Left(NotFound) when the backend returns NOT_FOUND" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.getVatCustomerInfo().futureValue

        result mustBe Left(NotFound)
      }
    }

    "must return Left(UnexpectedStatus) when the backend returns another error code" in {

      val status = Gen.oneOf(400, 500, 501, 502, 503).sample.value

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)))

        val result = connector.getVatCustomerInfo().futureValue

        result mustBe Left(UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
      }
    }
  }

  "getSavedExternalEntry" - {

    val url = s"/one-stop-shop-registration/external-entry"

    "must return information when the backend returns some" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val externalEntryResponse = ExternalEntryUrl(Some("/url"))

        val responseBody = Json.toJson(externalEntryResponse).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getSavedExternalEntry().futureValue

        result mustBe Right(externalEntryResponse)
      }
    }

    "must return invalid json when the backend returns some" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody = Json.obj("test" -> "test").toString()

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getSavedExternalEntry().futureValue

        result mustBe Right(ExternalEntryUrl(None))
      }
    }

    "must return Left(NotFound) when the backend returns NOT_FOUND" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.getSavedExternalEntry().futureValue

        result mustBe Left(NotFound)
      }
    }

    "must return Left(UnexpectedStatus) when the backend returns another error code" in {

      val status = Gen.oneOf(400, 500, 501, 502, 503).sample.value

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(aResponse().withStatus(status)))

        val result = connector.getSavedExternalEntry().futureValue

        result mustBe Left(UnexpectedResponseStatus(status, s"Received unexpected response code $status with body "))
      }
    }
  }

  "amendRegistration" - {
    val url = s"/one-stop-shop-registration/amend"

    "must return Right when a new registration is created on the backend" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(ok()))

        val result = connector.amendRegistration(registration).futureValue

        result mustBe Right(())
      }
    }

    "must return Left(UnexpectedResponseStatus) when the backend returns UnexpectedResponseStatus" in {

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(aResponse().withStatus(123)))

        val result = connector.amendRegistration(registration).futureValue

        result mustBe Left(UnexpectedResponseStatus(123, "Unexpected amend response, status 123 returned"))
      }
    }
  }

  ".enrolUser" - {

    "must return 204 when successful response" in {

      val url = s"/one-stop-shop-registration/confirm-enrolment"
      val app = application

      running(app) {
        val connector = app.injector.instanceOf[RegistrationConnector]

        server.stubFor(post(urlEqualTo(url)).willReturn(noContent()))

        val result = connector.enrolUser().futureValue

        result.status mustEqual NO_CONTENT
      }

    }
  }

  ".getIossRegistration" - {

    val iossUrl: String = "/ioss-registration/registration"

    "must return OK with a valid EtmpDisplayRegistration when IOSS backend returns OK with a valid response body" in {

      val etmpDisplayRegistration: EtmpDisplayRegistration = arbitraryEtmpDisplayRegistration.arbitrary.sample.value

      running(application) {

        val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

        val responseJson =
          s"""{
             | "exclusions" : ${Json.toJson(etmpDisplayRegistration.exclusions)}
             |}""".stripMargin

        server.stubFor(get(urlEqualTo(iossUrl))
          .willReturn(ok().withBody(responseJson))
        )

        val result = connector.getIossRegistration().futureValue

        result mustBe Right(etmpDisplayRegistration)
      }
    }

    "must return InvalidJson when IOSS backend returns invalid JSON" in {

      running(application) {

        val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]


        val responseJson = Json.obj("test" -> "test").toString()

        server.stubFor(get(urlEqualTo(iossUrl))
          .willReturn(ok().withBody(responseJson))
        )

        val result = connector.getIossRegistration().futureValue

        result mustBe Left(InvalidJson)
      }
    }

    "must return Left(UnexpectedStatus) when the backend returns another error code" in {

      val status = Gen.oneOf(BAD_REQUEST, INTERNAL_SERVER_ERROR, NOT_IMPLEMENTED, BAD_GATEWAY, SERVICE_UNAVAILABLE).sample.value

      running(application) {

        val connector: RegistrationConnector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(iossUrl))
          .willReturn(aResponse().withStatus(status))
        )

        val result = connector.getIossRegistration().futureValue

        result mustBe Left(UnexpectedResponseStatus(status, s"Unexpected IOSS registration response, status $status returned"))
      }
    }
  }
}