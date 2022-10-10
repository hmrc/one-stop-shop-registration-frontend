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
import com.github.tomakehurst.wiremock.client.WireMock.{aResponse, ok, post, urlEqualTo}
import models.core.{CoreRegistrationRequest, CoreRegistrationValidationResult, Match, MatchType, SourceType}
import models.responses.UnexpectedResponseStatus
import org.scalacheck.Gen
import play.api.Application
import play.api.libs.json.Json
import play.api.test.Helpers._
import testutils.WireMockHelper
import uk.gov.hmrc.domain.Vrn
import uk.gov.hmrc.http.HeaderCarrier

import java.time.LocalDate

class ValidateCoreRegistrationConnectorSpec extends SpecBase with WireMockHelper {

  implicit private lazy val hc: HeaderCarrier = HeaderCarrier()

  def getValidateCoreRegistrationUrl = s"/one-stop-shop-registration-stub/validateCoreRegistration"

  private def application: Application =
    applicationBuilder()
      .configure("microservice.services.core-validation.port" -> server.port)
      .build()

  val validCoreRegistrationResponse: CoreRegistrationValidationResult =
    CoreRegistrationValidationResult(
      searchId = "12345678",
      searchIdIntermediary = Some("12345678"),
      searchIdIssuedBy = "FR",
      traderFound = true,
      matches = Seq(
        Match(
          matchType = MatchType.FixedEstablishmentQuarantinedNETP,
          traderId = "444444444",
          intermediary = Some("IN4819283759"),
          memberState = "DE",
          exclusionStatusCode = Some(3),
          exclusionDecisionDate = Some(LocalDate.now().format(Match.dateFormatter)),
          exclusionEffectiveDate = Some(LocalDate.now().format(Match.dateFormatter)),
          nonCompliantReturns = Some(0),
          nonCompliantPayments = Some(0)
        )
      )
    )

  "validateCoreRegistration" - {

    "must return Right(CoreRegistrationValidationResult) when the server returns OK for a recognised payload" in {

      val vrn = Vrn("111111111")

      val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

      val validateCoreRegistration = validCoreRegistrationResponse

      val responseJson = Json.prettyPrint(Json.toJson(validateCoreRegistration))

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(ok(responseJson))
      )

      running(application) {
        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        result mustBe Right(validateCoreRegistration)
      }
    }

    "must return Left(UnexpectedStatus) when the server returns another error code" in {

      val vrn = Vrn("111111111")

      val coreRegistrationRequest = CoreRegistrationRequest(SourceType.VATNumber.toString, None, vrn.vrn, None, "GB")

      val status = Gen.oneOf(
        BAD_REQUEST,
        NOT_FOUND,
        METHOD_NOT_ALLOWED,
        NOT_ACCEPTABLE,
        UNSUPPORTED_MEDIA_TYPE,
        INTERNAL_SERVER_ERROR,
        BAD_GATEWAY,
        SERVICE_UNAVAILABLE
      ).sample.value

      server.stubFor(
        post(urlEqualTo(s"$getValidateCoreRegistrationUrl"))
          .willReturn(aResponse()
            .withStatus(status))
      )

      running(application) {
        val connector = application.injector.instanceOf[ValidateCoreRegistrationConnector]

        val result = connector.validateCoreRegistration(coreRegistrationRequest).futureValue

        result mustBe Left(
          UnexpectedResponseStatus(status, s"Received unexpected response code $status"))
      }
    }
  }

}
