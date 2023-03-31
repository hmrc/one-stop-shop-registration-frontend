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

import config.Service
import models.emails.EmailSendingResult.EMAIL_NOT_SENT
import models.emails.{EmailSendingResult, EmailToSendRequest}
import play.api.Configuration
import connectors.EmailHttpParser._
import logging.Logging
import uk.gov.hmrc.http.{BadGatewayException, GatewayTimeoutException, HeaderCarrier, HttpClient}

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject()(
  config: Configuration,
  client: HttpClient
) extends Logging {

  private val baseUrl = config.get[Service]("microservice.services.email")

  def send(email: EmailToSendRequest)
          (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[EmailSendingResult] = {
    client.POST[EmailToSendRequest, EmailSendingResult](
      s"${baseUrl}hmrc/email", email
    ).recover {
      case e: BadGatewayException =>
        logger.warn("There was an error sending the email: " + e.message + " " + e.responseCode)
        EMAIL_NOT_SENT
      case e: GatewayTimeoutException =>
        logger.warn("There was a time out whilst sending the email: " + e.message + " " + e.responseCode)
        EMAIL_NOT_SENT
    }
  }
}
