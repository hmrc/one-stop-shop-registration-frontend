/*
 * Copyright 2021 HM Revenue & Customs
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
import models.responses.ConflictFound
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
  }

  "get" - {

    "must return a registration when the backend returns one" in {

      val url = s"/one-stop-shop-registration/registration"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody = Json.toJson(RegistrationData.registration).toString

        server.stubFor(get(urlEqualTo(url)).willReturn(ok().withBody(responseBody)))

        val result = connector.getRegistration(RegistrationData.registration.vrn).futureValue

        result.value mustEqual RegistrationData.registration
      }
    }

    "must return None when the backend returns NOT_FOUND" in {

      val url = s"/one-stop-shop-registration/registration"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        server.stubFor(get(urlEqualTo(url)).willReturn(notFound()))

        val result = connector.getRegistration(RegistrationData.registration.vrn).futureValue

        result must not be defined
      }
    }
  }
}
