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

package connectors

import config.FrontendAppConfig
import connectors.ValidateCoreRegistrationHttpParser.{ValidateCoreRegistrationReads, ValidateCoreRegistrationResponse}
import logging.Logging
import models.core.{CoreRegistrationRequest, EisErrorResponse}
import models.responses.EisError
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpException}

import java.time.Instant
import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateCoreRegistrationConnector @Inject()(
                                                   frontendAppConfig: FrontendAppConfig,
                                                   httpClient: HttpClient
                                                 )(implicit ec: ExecutionContext) extends HttpErrorFunctions with Logging {

  private val baseUrl = frontendAppConfig.coreValidationUrl

  def validateCoreRegistration(
                                coreRegistrationRequest: CoreRegistrationRequest
                              )(implicit hc: HeaderCarrier): Future[ValidateCoreRegistrationResponse] = {

    val url = s"$baseUrl/validate-core-registration"
    httpClient.POST[CoreRegistrationRequest, ValidateCoreRegistrationResponse](
      url,
      coreRegistrationRequest
    ).recover {
      case e: HttpException =>
        logger.error(
          s"Unexpected error response from backend"
        )
        Left(EisError(
          EisErrorResponse(Instant.now(), s"UNEXPECTED_${e.responseCode.toString}", e.message)
        ))

      case e =>
        logger.error(
          s"Unexpected error response from backend"
        )
        Left(EisError(
          EisErrorResponse(Instant.now(), "UNEXPECTED", e.getMessage)
        ))
    }
  }

}

