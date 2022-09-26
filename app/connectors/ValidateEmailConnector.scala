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
import logging.Logging
import models.{ValidateEmailRequest, ValidateEmailResponse}
import play.api.Configuration
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpException}
import connectors.ValidateEmailHttpParser.{ReturnValidateEmailReads, ReturnValidateEmailResponse}
import models.responses.UnexpectedResponseStatus

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class ValidateEmailConnector @Inject()(
                                      config: Configuration,
                                      client: HttpClient
                                      ) extends Logging {

  private val baseUrl = config.get[Service]("microservice.services.email-verification")

  def verifyEmail(validateEmailRequest: ValidateEmailRequest)
                 (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[ValidateEmailResponse] = {
    val url = s"${baseUrl}hmrc/verify-email"
    client.POST[ValidateEmailRequest, ReturnValidateEmailResponse](url, validateEmailRequest)
      .recover {
      case e: HttpException =>
        logger.error(s"ValidateEmailResponse received an unexpected error wirth status: ${e.responseCode}")
        Left(UnexpectedResponseStatus(e.responseCode, s"Unexpected response, status ${e.responseCode} returned"))
    }
  }

}
