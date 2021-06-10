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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, equalToJson, post, urlEqualTo}
import models.emails._
import org.apache.http.HttpStatus
import play.api.Application
import play.api.test.Helpers.running
import testutils.WireMockHelper
import uk.gov.hmrc.http.HeaderCarrier
import utils.ResourceFiles
import utils.JsonFormatters._

class EmailConnectorSpec extends SpecBase with WireMockHelper with ResourceFiles {
  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.hmrc-email-renderer.port" -> server.port)
      .build()

  private val emailParams = RegistrationConfirmationEmailParameters("VRN")
  private val email = RegistrationConfirmationEmail(Seq("user@domain.com"), emailParams)
  private val hmrcEmailRendererUrl = s"/hmrc-email-renderer/templates/${EmailType.REGISTRATION_CONFIRMATION}"

  "EmailConnector.generate" - {

    "should return an email template when called with parameters" in {
      running(application) {
        val connector = application.injector.instanceOf[EmailConnector]

        server.stubFor(
          post(urlEqualTo(hmrcEmailRendererUrl))
            .withRequestBody(equalToJson(fromResource("emails/email_template-request.json")))
            .willReturn(
              aResponse()
                .withBody(fromResource("emails/email_template-response.json"))
                .withStatus(HttpStatus.SC_OK)
            )
        )

        connector.generate(email).futureValue mustBe EmailTemplate("text", "html", "from", "subject", "service")
      }
    }
  }
}
