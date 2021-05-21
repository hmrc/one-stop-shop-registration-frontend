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
import com.github.tomakehurst.wiremock.client.WireMock.{ok, post, urlEqualTo}
import models.RegistrationResponse
import models.requests.RegistrationRequest
import play.api.Application
import play.api.test.Helpers.running
import testutils.{RegistrationData, WireMockHelper}
import uk.gov.hmrc.http.HeaderCarrier

class RegistrationRequestConnectorSpec extends SpecBase with WireMockHelper {

  val registration = RegistrationData.createNewRegistration()

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.one-stop-shop-registration.port" -> server.port)
      .build()

  "submitRegistration" - {

    "must return OK when a new registration is created on the backend" in {
      val request = RegistrationRequest(
        registration.registeredCompanyName,
        registration.hasTradingName,
        registration.tradingNames,
        registration.partOfVatGroup,
        registration.ukVatNumber,
        registration.ukVatEffectiveDate,
        registration.ukVatRegisteredPostcode,
        registration.vatRegisteredInEu,
        registration.euVatDetails,
        registration.businessAddress,
        registration.businessContactDetails,
        registration.websites
      )

      val url = s"/one-stop-shop-registration/create"

      running(application) {
        val connector = application.injector.instanceOf[RegistrationConnector]

        val responseBody =
          """{"registrationCreated": true}"""

        server.stubFor(post(urlEqualTo(url))
          .willReturn(ok(responseBody)))

        val result = connector.submitRegistration(request).futureValue

        result mustEqual RegistrationResponse(true)

      }
    }
  }

}
