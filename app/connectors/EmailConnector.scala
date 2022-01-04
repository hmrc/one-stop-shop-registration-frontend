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

import com.google.inject.Inject
import config.Service
import models.emails.EmailSendingResult.{EMAIL_ACCEPTED, EMAIL_NOT_SENT, EMAIL_UNSENDABLE}
import models.emails.{EmailSendingResult, EmailToSendRequest}
import uk.gov.hmrc.http.{HeaderCarrier, HttpClient, HttpReads, HttpResponse}
import play.api.Configuration

import javax.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class EmailConnector @Inject()(
  config: Configuration,
  client: HttpClient
)(implicit ec: ExecutionContext) {

  private val baseUrl = config.get[Service]("microservice.services.email")
  private val identityReads: HttpReads[HttpResponse] = (_: String, _: String, response: HttpResponse) => response

  def send(email: EmailToSendRequest)
          (implicit ec: ExecutionContext, hc: HeaderCarrier): Future[EmailSendingResult] = {
    client.POST[EmailToSendRequest, HttpResponse](
      s"${baseUrl}hmrc/email", email
    )(implicitly, identityReads, implicitly, implicitly).map {
      case r if r.status >= 200 && r.status < 300 =>
        EMAIL_ACCEPTED
      case r if r.status >= 400 && r.status < 500 =>
        EMAIL_UNSENDABLE
      case r if r.status >= 500 && r.status < 600 =>
        EMAIL_NOT_SENT
      case r =>
        EMAIL_ACCEPTED
    }
  }
}
