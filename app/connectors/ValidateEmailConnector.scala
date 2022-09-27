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

import config.Service
import connectors.ValidateEmailHttpParser.{ReturnValidateEmailReads, ReturnValidateEmailResponse}
import logging.Logging
import models.ValidateEmailRequest
import models.responses.UnexpectedResponseStatus
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpErrorFunctions, HttpException}

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateEmailConnector @Inject()(
                                        config: Configuration,
                                        httpClient: HttpClient
                                      ) extends HttpErrorFunctions with Logging {

  private val baseUrl = config.get[Service]("microservice.services.email-verification")

  def validateEmail(validateEmailRequest: ValidateEmailRequest)
                   (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ReturnValidateEmailResponse] = {
    val url = s"${baseUrl}/verify-email"
    httpClient.POST[ValidateEmailRequest, ReturnValidateEmailResponse](url, validateEmailRequest)
      .recover {
        case e: HttpException =>
          logger.error(s"ValidateEmailResponse received an unexpected error wirth status: ${e.responseCode}")
          Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response, status ${e.responseCode} returned"))
      }
  }

}
